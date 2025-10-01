/**
 * Copyright 2025 Sk Niyaj Ali
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.niyajali.clipboard.manager

/**
 * Represents the result of a clipboard operation.
 *
 * This sealed interface provides a type-safe way to handle both successful
 * clipboard operations and various types of failures that can occur.
 *
 * **Usage Example:**
 * ```kotlin
 * when (val result = clipboardManager.readText()) {
 *     is ClipboardResult.Success -> println("Text: ${result.data}")
 *     is ClipboardResult.Empty -> println("Clipboard is empty")
 *     is ClipboardResult.PermissionDenied -> println("Permission required")
 *     is ClipboardResult.Error -> println("Error: ${result.message}")
 * }
 * ```
 *
 * @param T The type of data returned on success
 * @see ClipboardManager
 * @since 1.0.0
 */
public sealed interface ClipboardResult<out T> {
    /**
     * Represents a successful clipboard operation.
     *
     * @property data The clipboard data retrieved from the operation
     */
    public data class Success<T>(val data: T) : ClipboardResult<T>

    /**
     * Represents an empty clipboard.
     *
     * The clipboard exists but contains no content of the requested type.
     */
    public data object Empty : ClipboardResult<Nothing>

    /**
     * Represents a permission denial.
     *
     * This typically occurs on web platforms (JS/WasmJS) when the user
     * hasn't granted clipboard access permission.
     *
     * @property message Optional message explaining the permission issue
     */
    public data class PermissionDenied(val message: String? = null) : ClipboardResult<Nothing>

    /**
     * Represents an error during clipboard operation.
     *
     * @property message Description of the error
     * @property cause The underlying exception, if available
     */
    public data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : ClipboardResult<Nothing>

    /**
     * Companion object providing utility functions for creating results.
     */
    public companion object {
        /**
         * Creates a success result from nullable data.
         *
         * @param data The data to wrap, or null to return [Empty]
         * @return [Success] if data is non-null, [Empty] otherwise
         */
        public fun <T : Any> ofNullable(data: T?): ClipboardResult<T> = data?.let { Success(it) } ?: Empty

        /**
         * Creates a result from a standard Kotlin [Result].
         *
         * @param result The Kotlin result to convert
         * @return [Success] if result is success, [Error] otherwise
         */
        public fun <T> from(result: Result<T>): ClipboardResult<T> = result.fold(
            onSuccess = { Success(it) },
            onFailure = { Error(it.message ?: "Unknown error", it) },
        )
    }
}

/**
 * Maps the success value of this [ClipboardResult] using the provided transform function.
 *
 * @param transform The function to apply to the success value
 * @return A new [ClipboardResult] with the transformed value, or the original error
 */
public inline fun <T, R> ClipboardResult<T>.map(
    transform: (T) -> R,
): ClipboardResult<R> = when (this) {
    is ClipboardResult.Success -> ClipboardResult.Success(transform(data))
    is ClipboardResult.Empty -> ClipboardResult.Empty
    is ClipboardResult.PermissionDenied -> this
    is ClipboardResult.Error -> this
}

/**
 * Maps the success value of this [ClipboardResult] using a transform that returns another [ClipboardResult].
 *
 * @param transform The function to apply to the success value
 * @return The result of the transform function, or the original error
 */
public inline fun <T, R> ClipboardResult<T>.flatMap(
    transform: (T) -> ClipboardResult<R>,
): ClipboardResult<R> = when (this) {
    is ClipboardResult.Success -> transform(data)
    is ClipboardResult.Empty -> ClipboardResult.Empty
    is ClipboardResult.PermissionDenied -> this
    is ClipboardResult.Error -> this
}

/**
 * Returns the success value or null if this is not a success result.
 *
 * @return The data if this is [ClipboardResult.Success], null otherwise
 */
public fun <T> ClipboardResult<T>.getOrNull(): T? = when (this) {
    is ClipboardResult.Success -> data
    else -> null
}

/**
 * Returns the success value or the provided default if this is not a success result.
 *
 * @param default The default value to return on failure
 * @return The data if this is [ClipboardResult.Success], the default otherwise
 */
public fun <T> ClipboardResult<T>.getOrDefault(default: T): T = when (this) {
    is ClipboardResult.Success -> data
    else -> default
}

/**
 * Returns the success value or throws an exception if this is not a success result.
 *
 * @return The data if this is [ClipboardResult.Success]
 * @throws IllegalStateException if this is not a success result
 */
public fun <T> ClipboardResult<T>.getOrThrow(): T = when (this) {
    is ClipboardResult.Success -> data
    is ClipboardResult.Empty -> throw IllegalStateException("Clipboard is empty")
    is ClipboardResult.PermissionDenied -> throw IllegalStateException("Permission denied: $message")
    is ClipboardResult.Error -> throw IllegalStateException(message, cause)
}

/**
 * Executes the given block if this is a success result.
 *
 * @param block The block to execute with the success value
 * @return This result for chaining
 */
public inline fun <T> ClipboardResult<T>.onSuccess(block: (T) -> Unit): ClipboardResult<T> {
    if (this is ClipboardResult.Success) block(data)
    return this
}

/**
 * Executes the given block if this is an empty result.
 *
 * @param block The block to execute
 * @return This result for chaining
 */
public inline fun <T> ClipboardResult<T>.onEmpty(block: () -> Unit): ClipboardResult<T> {
    if (this is ClipboardResult.Empty) block()
    return this
}

/**
 * Executes the given block if this is a permission denied result.
 *
 * @param block The block to execute with the optional message
 * @return This result for chaining
 */
public inline fun <T> ClipboardResult<T>.onPermissionDenied(block: (String?) -> Unit): ClipboardResult<T> {
    if (this is ClipboardResult.PermissionDenied) block(message)
    return this
}

/**
 * Executes the given block if this is an error result.
 *
 * @param block The block to execute with the error message and optional cause
 * @return This result for chaining
 */
public inline fun <T> ClipboardResult<T>.onError(block: (String, Throwable?) -> Unit): ClipboardResult<T> {
    if (this is ClipboardResult.Error) block(message, cause)
    return this
}

/**
 * Checks if this result represents a successful operation.
 *
 * @return true if this is [ClipboardResult.Success], false otherwise
 */
public fun <T> ClipboardResult<T>.isSuccess(): Boolean = this is ClipboardResult.Success

/**
 * Checks if this result represents a failure (any non-success result).
 *
 * @return true if this is not [ClipboardResult.Success], false otherwise
 */
public fun <T> ClipboardResult<T>.isFailure(): Boolean = this !is ClipboardResult.Success

/**
 * Checks if this result represents an empty clipboard.
 *
 * @return true if this is [ClipboardResult.Empty], false otherwise
 */
public fun <T> ClipboardResult<T>.isEmpty(): Boolean = this is ClipboardResult.Empty

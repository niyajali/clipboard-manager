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

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized

/**
 * Manages the collection of available platform implementations.
 *
 * Maintains a thread-safe registry of platform implementations and selects
 * the most appropriate one based on priority and platform compatibility.
 * Used internally by [ClipboardMonitorFactory].
 *
 * @since 1.0.0
 */
@OptIn(InternalCoroutinesApi::class)
internal class PlatformRegistry {
    private val strategies = mutableListOf<PlatformStrategy>()
    private val lock = SynchronizedObject()

    /**
     * Adds a platform implementation to the registry.
     *
     * Implementations are automatically sorted by priority after registration,
     * with higher priority implementations checked first during selection.
     *
     * @param strategy The platform implementation to register
     */
    fun register(strategy: PlatformStrategy) {
        synchronized(lock) {
            strategies.add(strategy)
            strategies.sortByDescending { it.priority }
        }
    }

    /**
     * Removes a platform implementation from the registry.
     *
     * @param strategy The platform implementation to remove
     * @return true if the implementation was removed, false if it wasn't registered
     */
    fun unregister(strategy: PlatformStrategy): Boolean {
        synchronized(lock) {
            return strategies.remove(strategy)
        }
    }

    /**
     * Selects the best platform implementation for the current environment.
     *
     * Evaluates all registered implementations in priority order and returns
     * the first one that reports itself as applicable. Returns null if no
     * suitable implementation is found.
     *
     * @return The best available platform implementation, or null if none are suitable
     */
    fun selectStrategy(): PlatformStrategy? {
        synchronized(lock) {
            return strategies.firstOrNull { it.isApplicable() }
        }
    }

    /**
     * Returns all registered platform implementations.
     *
     * @return An immutable list of all registered implementations, ordered by priority
     */
    fun getAllStrategies(): List<PlatformStrategy> {
        synchronized(lock) {
            return strategies.toList()
        }
    }

    /**
     * Removes all registered platform implementations.
     *
     * Primarily used for testing. After calling this, [selectStrategy] will
     * return null until new implementations are registered.
     */
    fun clear() {
        synchronized(lock) {
            strategies.clear()
        }
    }

    /**
     * Checks if any platform implementations are registered.
     *
     * @return true if at least one implementation is registered
     */
    fun isEmpty(): Boolean {
        synchronized(lock) {
            return strategies.isEmpty()
        }
    }

    /**
     * Returns the number of registered platform implementations.
     *
     * @return The count of registered implementations
     */
    fun size(): Int {
        synchronized(lock) {
            return strategies.size
        }
    }
}

package com.niyajali.clipboard.manager

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Extension function to observe clipboard changes as a Kotlin Flow.
 *
 * This function creates a cold Flow that starts monitoring the clipboard when collected
 * and stops when the collection is cancelled. The Flow automatically handles the
 * lifecycle of the [ClipboardMonitor].
 *
 * **Usage Example:**
 * ```kotlin
 * ClipboardMonitorFactory.create(listener)
 *     .asFlow()
 *     .collect { content ->
 *         println("Clipboard: ${content.text}")
 *     }
 * ```
 *
 * **Features:**
 * - Automatically starts/stops monitoring based on collection lifecycle
 * - Emits initial clipboard content immediately upon collection
 * - Thread-safe and cancellation-safe
 * - Integrates seamlessly with Kotlin coroutines
 *
 * @return A Flow that emits [ClipboardContent] whenever the clipboard changes.
 *         The Flow is cold and starts monitoring on collection.
 *
 * @see ClipboardMonitor
 * @see ClipboardContent
 * @since 1.0.0
 */
public fun ClipboardMonitor.asFlow(): Flow<ClipboardContent> = callbackFlow {
    val listener = object : ClipboardListener {
        override fun onClipboardChange(content: ClipboardContent) {
            trySend(content)
        }
    }

    // Create a new monitor with our listener
    val factory = ClipboardMonitorFactory
    val monitor = factory.create(listener)

    monitor.start()

    awaitClose {
        monitor.stop()
    }
}

/**
 * Extension function to observe only text changes in the clipboard.
 *
 * This is a convenience function that filters [ClipboardContent] to emit only
 * when text content is available and has changed.
 *
 * **Usage Example:**
 * ```kotlin
 * ClipboardMonitorFactory.create(listener)
 *     .textFlow()
 *     .collect { text ->
 *         println("Text copied: $text")
 *     }
 * ```
 *
 * @param includeEmpty If true, emits null when clipboard is cleared or has no text.
 *                     If false (default), only emits when text is present.
 *
 * @return A Flow that emits text content whenever it changes in the clipboard.
 *
 * @see asFlow
 * @since 1.0.0
 */
public fun ClipboardMonitor.textFlow(includeEmpty: Boolean = false): Flow<String?> =
    asFlow()
        .map { it.text }
        .let { flow ->
            if (includeEmpty) flow else flow.map { it?.takeIf { text -> text.isNotEmpty() } }
        }
        .distinctUntilChanged()

/**
 * Extension function to observe file changes in the clipboard.
 *
 * Emits whenever files are copied to the clipboard. Useful for file manager
 * integrations or drag-and-drop operations.
 *
 * **Usage Example:**
 * ```kotlin
 * ClipboardMonitorFactory.create(listener)
 *     .filesFlow()
 *     .collect { files ->
 *         println("Files copied: ${files.joinToString()}")
 *     }
 * ```
 *
 * @return A Flow that emits file lists whenever they change in the clipboard.
 *         Only emits when files are present (empty lists are filtered out).
 *
 * @see asFlow
 * @since 1.0.0
 */
public fun ClipboardMonitor.filesFlow(): Flow<List<String>> =
    asFlow()
        .map { it.files }
        .distinctUntilChanged()
        .map { it ?: emptyList() }
        .distinctUntilChanged { old, new -> old.isEmpty() && new.isEmpty() }

/**
 * Extension function to observe image availability in the clipboard.
 *
 * Emits a boolean whenever the presence of an image in the clipboard changes.
 * Note that this only indicates availability; actual image data must be
 * retrieved through platform-specific APIs.
 *
 * **Usage Example:**
 * ```kotlin
 * ClipboardMonitorFactory.create(listener)
 *     .imageAvailableFlow()
 *     .collect { hasImage ->
 *         if (hasImage) {
 *             println("Image available in clipboard")
 *         }
 *     }
 * ```
 *
 * @return A Flow that emits true when an image is available, false otherwise.
 *
 * @see asFlow
 * @since 1.0.0
 */
public fun ClipboardMonitor.imageAvailableFlow(): Flow<Boolean> =
    asFlow()
        .map { it.imageAvailable }
        .distinctUntilChanged()

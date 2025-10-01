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
 * Platform-agnostic interface for monitoring system clipboard changes.
 *
 * A [ClipboardMonitor] tracks changes to the system clipboard and notifies registered
 * listeners when new content is copied or the clipboard is cleared. The monitor must
 * be explicitly started with [start] and should be stopped with [stop] when no longer needed.
 *
 * **Lifecycle:**
 * ```kotlin
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start() // Begin monitoring
 * // ... use the monitor ...
 * monitor.stop()  // Stop monitoring and release resources
 * ```
 *
 * **Platform-Specific Behavior:**
 * - **Android**: Uses `ClipboardManager.OnPrimaryClipChangedListener` with automatic debouncing
 * - **Windows**: Uses native Win32 clipboard change notifications via message-only window
 * - **macOS/Linux**: Uses AWT clipboard polling with configurable interval
 * - **JS/WasmJS**: Uses browser Clipboard API with periodic polling
 *
 * **Thread Safety:**
 * All methods are thread-safe and can be called from any thread. The [ClipboardListener]
 * callbacks may be invoked on platform-specific threads.
 *
 * @see ClipboardMonitorFactory
 * @see ClipboardListener
 * @since 1.0.0
 */
public interface ClipboardMonitor {
    /**
     * Starts monitoring the clipboard for changes.
     *
     * After calling this method, the monitor will begin tracking clipboard changes
     * and will invoke the registered [ClipboardListener] whenever the clipboard content
     * changes. The current clipboard content is typically read and reported immediately
     * upon starting.
     *
     * **Important Notes:**
     * - Calling [start] on an already running monitor has no effect (idempotent)
     * - On Android, ensure [ClipboardMonitorFactory.init] was called first
     * - The monitor continues running until [stop] is called
     * - Resources are allocated during start; always call [stop] to release them
     *
     * @throws IllegalStateException on Android if [ClipboardMonitorFactory.init] was not called
     * @throws IllegalStateException on JVM/Windows if window creation fails
     *
     * @see stop
     * @see isRunning
     */
    public fun start()

    /**
     * Stops monitoring the clipboard and releases all associated resources.
     *
     * After calling this method, the monitor will no longer track clipboard changes
     * and will not invoke the listener. All background threads, event listeners, and
     * native resources are cleaned up.
     *
     * **Important Notes:**
     * - Calling [stop] on an already stopped monitor has no effect (idempotent)
     * - After stopping, the monitor can be restarted by calling [start]
     * - Always call this method when done monitoring to prevent resource leaks
     * - On Android, this removes the clipboard change listener
     * - On JVM, this shuts down background threads and unregisters native hooks
     *
     * @see start
     * @see isRunning
     */
    public fun stop()

    /**
     * Checks if the monitor is currently running.
     *
     * @return `true` if the monitor is actively tracking clipboard changes, `false` otherwise
     *
     * @see start
     * @see stop
     */
    public fun isRunning(): Boolean

    /**
     * Retrieves the current clipboard content without starting continuous monitoring.
     *
     * This is a synchronous method that reads the current clipboard state and returns
     * it immediately. It can be called whether the monitor is running or not.
     *
     * **Use Cases:**
     * - One-time clipboard content retrieval
     * - Checking clipboard state before starting monitoring
     * - Polling clipboard manually without automatic change detection
     *
     * **Platform Notes:**
     * - **Android**: Reads from `ClipboardManager.primaryClip`
     * - **JVM**: Reads from `Toolkit.getDefaultToolkit().systemClipboard`
     * - **JS/WasmJS**: Reads from `navigator.clipboard` (requires user permission)
     *
     * @return The current clipboard content. Fields may be null if specific content types
     *         are not available or the clipboard is empty.
     *
     * @see ClipboardContent
     */
    public fun getCurrentContent(): ClipboardContent
}

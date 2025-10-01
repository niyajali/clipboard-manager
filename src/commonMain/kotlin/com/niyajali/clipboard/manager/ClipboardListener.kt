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
 * Listener interface for receiving clipboard change notifications.
 *
 * Implementations of this interface will be notified whenever the system clipboard
 * content changes. The listener is called on a background thread (platform-dependent),
 * so implementations should handle thread-safety appropriately.
 *
 * **Threading Notes:**
 * - **Android**: Called on the main thread (UI thread)
 * - **JVM (Windows)**: Called on a dedicated message loop thread
 * - **JVM (macOS/Linux)**: Called on a scheduled executor thread
 * - **JS/WasmJS**: Called on the event loop (main thread equivalent)
 *
 * **Important**: Exceptions thrown in [onClipboardChange] are caught internally
 * and will not stop the clipboard monitoring.
 *
 * @see ClipboardMonitor
 * @see ClipboardMonitorFactory.create
 * @since 1.0.0
 */
public interface ClipboardListener {
    /**
     * Called when the clipboard content changes.
     *
     * This method is invoked whenever new content is copied to the clipboard or
     * the clipboard is cleared. The method may be called multiple times with the
     * same content on some platforms if clipboard change events are fired repeatedly.
     *
     * **Implementation Guidelines:**
     * - Keep this method lightweight; heavy processing should be offloaded to background threads
     * - Handle null/empty content gracefully
     * - Do not throw exceptions (they will be caught and ignored)
     * - Be thread-safe if accessing shared state
     *
     * @param content The current clipboard content. May contain null fields if specific
     *                content types are not available.
     *
     * @see ClipboardContent
     */
    public fun onClipboardChange(content: ClipboardContent)
}

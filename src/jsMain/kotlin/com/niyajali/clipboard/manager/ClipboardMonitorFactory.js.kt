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
 * JavaScript/Browser-specific implementation of [ClipboardMonitorFactory].
 *
 * This factory creates [JSClipboardMonitor] instances that use the browser's
 * Clipboard API (`navigator.clipboard`) to monitor clipboard changes.
 *
 * **Browser Requirements:**
 * - Secure context (HTTPS or localhost)
 * - Modern browser with Clipboard API support
 * - User permission for clipboard access
 *
 * **Usage Example:**
 * ```kotlin
 * val listener = object : ClipboardListener {
 *     override fun onClipboardChange(content: ClipboardContent) {
 *         console.log("Clipboard text: ${content.text}")
 *     }
 * }
 *
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start()
 * ```
 *
 * **Checking Browser Support:**
 * ```kotlin
 * // Check if Clipboard API is available
 * val isSupported = js("typeof navigator.clipboard !== 'undefined'") as Boolean
 *
 * if (isSupported) {
 *     val monitor = ClipboardMonitorFactory.create(listener)
 *     monitor.start()
 * } else {
 *     console.error("Clipboard API not supported")
 * }
 * ```
 *
 * **Limitations:**
 * - Polling-based (no native change events)
 * - Requires user permission
 * - Only supports text content reliably
 * - HTML and file support varies by browser
 * - RTF not supported
 *
 * @see JSClipboardMonitor
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Clipboard_API">MDN Clipboard API</a>
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    /**
     * Creates a new [JSClipboardMonitor] instance.
     *
     * The monitor uses polling to detect clipboard changes as browsers don't
     * provide native change events for the clipboard.
     *
     * **Permission:** The browser will prompt the user for clipboard read
     * permission when the monitor first accesses the clipboard.
     *
     * @param listener The listener that will receive clipboard change notifications
     *
     * @return A new [JSClipboardMonitor] instance in a stopped state
     *
     * @see JSClipboardMonitor
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor = JSClipboardMonitor(listener)
}

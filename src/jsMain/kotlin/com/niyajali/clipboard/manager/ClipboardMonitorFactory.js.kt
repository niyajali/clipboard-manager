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
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return JSClipboardMonitor(listener)
    }
}

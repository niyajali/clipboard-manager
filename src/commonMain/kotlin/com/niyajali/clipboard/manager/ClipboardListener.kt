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

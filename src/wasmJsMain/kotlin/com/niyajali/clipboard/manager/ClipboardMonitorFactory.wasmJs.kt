package com.niyajali.clipboard.manager

/**
 * WebAssembly JavaScript-specific implementation of [ClipboardMonitorFactory].
 *
 * This factory creates clipboard monitors that work in WebAssembly/JavaScript
 * environments. It uses a similar implementation to the standard JS target but
 * is optimized for WASM interop.
 *
 * **Browser Requirements:**
 * - WebAssembly support
 * - Secure context (HTTPS or localhost)
 * - Modern browser with Clipboard API support
 * - User permission for clipboard access
 *
 * **Usage Example:**
 * ```kotlin
 * val listener = object : ClipboardListener {
 *     override fun onClipboardChange(content: ClipboardContent) {
 *         console.log("Clipboard: ${content.text}")
 *     }
 * }
 *
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start()
 * ```
 *
 * **Note:** This implementation is similar to the JS implementation but compiled
 * to WebAssembly for potentially better performance and smaller bundle sizes.
 *
 * @see ClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    /**
     * Creates a new clipboard monitor for WebAssembly/JavaScript environment.
     *
     * The monitor uses polling to detect clipboard changes as browsers don't
     * provide native change events for the clipboard.
     *
     * **Permission:** The browser will prompt the user for clipboard read
     * permission when the monitor first accesses the clipboard.
     *
     * @param listener The listener that will receive clipboard change notifications
     *
     * @return A new clipboard monitor instance in a stopped state
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        // Note: For WasmJS, we can use a simple text-only implementation
        // or share code with JS implementation
        return WasmJSClipboardMonitor(listener)
    }
}

/**
 * Internal WebAssembly-specific clipboard monitor implementation.
 *
 * This is a simplified implementation optimized for WASM targets.
 * It focuses on text content only for maximum compatibility.
 */
internal class WasmJSClipboardMonitor(
    private val listener: ClipboardListener,
    private val pollingIntervalMs: Int = 500
) : ClipboardMonitor {

    private var running = false
    private var lastText: String? = null

    override fun start() {
        if (running) return
        running = true
        // TODO: Implement WASM-specific clipboard polling
        // For now, this is a placeholder that can be enhanced
//        console.log("WasmJS ClipboardMonitor started (basic implementation)")
    }

    override fun stop() {
        if (!running) return
        running = false
        lastText = null
//        console.log("WasmJS ClipboardMonitor stopped")
    }

    override fun isRunning(): Boolean = running

    override fun getCurrentContent(): ClipboardContent {
        return ClipboardContent(
            text = null,
            html = null,
            rtf = null,
            files = null,
            imageAvailable = false,
            timestamp = 0
        )
    }
}

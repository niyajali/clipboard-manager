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
 * JavaScript/Browser platform implementation using the Clipboard API.
 */
private class JSStrategy : PlatformStrategy {
    override val priority: Int = 100

    override fun isApplicable(): Boolean {
        return try {
            js("typeof navigator !== 'undefined' && navigator.clipboard !== undefined") as Boolean
        } catch (e: Exception) {
            false
        }
    }

    override fun createMonitor(config: ClipboardConfig): ClipboardMonitor {
        return JSClipboardMonitor(
            listener = config.listener,
            pollingIntervalMs = config.pollingIntervalMs.toInt(),
            enableDuplicateFiltering = config.enableDuplicateFiltering,
            errorHandler = config.errorHandler,
        )
    }
}

/**
 * Creates clipboard monitors for JavaScript/Browser environments.
 *
 * Uses the browser's Clipboard API to monitor clipboard changes.
 * Requires a secure context (HTTPS or localhost) and user permission.
 *
 * **Browser Requirements:**
 * - Secure context (HTTPS or localhost)
 * - Clipboard API support (Chrome 88+, Firefox 90+, Safari 13.1+)
 * - User permission for clipboard read access
 *
 * **Limitations:**
 * - No native change events (uses polling)
 * - RTF format not supported
 * - File access limited by browser security
 * - Permission prompt on first access
 *
 * Example:
 * ```kotlin
 * if (window.navigator.asDynamic().clipboard != null) {
 *     val monitor = ClipboardMonitor.Builder()
 *         .setListener(listener)
 *         .setPollingInterval(500)
 *         .build()
 *     monitor.start()
 * } else {
 *     console.error("Clipboard API not available")
 * }
 * ```
 *
 * @see JSClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    internal actual val registry: PlatformRegistry = PlatformRegistry()

    init {
        registry.register(JSStrategy())
    }

    /**
     * Creates a clipboard monitor with the specified configuration.
     *
     * @param config The configuration for the monitor
     * @return A new JavaScript clipboard monitor
     * @throws UnsupportedOperationException if Clipboard API is not available
     */
    public actual fun create(config: ClipboardConfig): ClipboardMonitor {
        val strategy = registry.selectStrategy()
            ?: throw UnsupportedOperationException(
                "Clipboard API not available. " +
                    "Ensure you're running in a secure context (HTTPS or localhost) " +
                    "and the browser supports the Clipboard API.",
            )

        return strategy.createMonitor(config)
    }

    /**
     * Creates a clipboard monitor with default configuration.
     *
     * @param listener The listener to receive clipboard change notifications
     * @return A new Android clipboard monitor with default settings
     * @throws IllegalStateException if the factory has not been initialized
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return create(ClipboardConfig.default(listener))
    }
}

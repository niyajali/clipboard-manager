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
 * iOS platform implementation using UIPasteboard.
 */
private class IOSStrategy : PlatformStrategy {
    override val priority: Int = 100

    override fun isApplicable(): Boolean = true

    override fun createMonitor(config: ClipboardConfig): ClipboardMonitor {
        return IOSClipboardMonitor(
            listener = config.listener,
            pollingIntervalMs = config.pollingIntervalMs / 1000.0,
            enableDuplicateFiltering = config.enableDuplicateFiltering,
            errorHandler = config.errorHandler,
        )
    }
}

/**
 * Creates clipboard monitors for iOS.
 *
 * Uses UIPasteboard to monitor clipboard changes on iOS devices.
 * No initialization required.
 *
 * **Platform Details:**
 * - Uses UIPasteboard.general for system clipboard access
 * - Monitors changeCount property for efficient change detection
 * - Callbacks delivered on the main thread
 *
 * **Permissions:**
 * iOS 14+ shows a paste notification banner on first clipboard access.
 * No explicit permission required.
 *
 * Example:
 * ```kotlin
 * val monitor = ClipboardMonitor.Builder()
 *     .setListener(listener)
 *     .setPollingInterval(500)
 *     .build()
 * monitor.start()
 * ```
 *
 * @see IOSClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    internal actual val registry: PlatformRegistry = PlatformRegistry()

    init {
        registry.register(IOSStrategy())
    }

    /**
     * Creates a clipboard monitor with the specified configuration.
     *
     * @param config The configuration for the monitor
     * @return A new iOS clipboard monitor
     */
    public actual fun create(config: ClipboardConfig): ClipboardMonitor {
        val strategy = registry.selectStrategy()
            ?: throw UnsupportedOperationException(
                "No applicable clipboard strategy found for iOS/Native platform.",
            )

        return strategy.createMonitor(config)
    }

    /**
     * Creates a clipboard monitor with default configuration.
     *
     * @param listener The listener to receive clipboard change notifications
     * @return A new iOS clipboard monitor with default settings
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return create(ClipboardConfig.default(listener))
    }
}

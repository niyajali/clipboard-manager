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

import com.niyajali.clipboard.manager.windows.WindowsClipboardMonitor
import com.sun.jna.Platform

/**
 * Windows platform implementation using native Win32 clipboard notifications.
 */
private class WindowsStrategy : PlatformStrategy {
    override val priority: Int = 100

    override fun isApplicable(): Boolean = Platform.isWindows()

    override fun createMonitor(config: ClipboardConfig): ClipboardMonitor {
        return WindowsClipboardMonitor(
            listener = config.listener,
            enableDuplicateFiltering = config.enableDuplicateFiltering,
            errorHandler = config.errorHandler,
        )
    }
}

/**
 * Generic AWT implementation for macOS and Linux using polling.
 */
private class AwtStrategy : PlatformStrategy {
    override val priority: Int = 50

    override fun isApplicable(): Boolean {
        return try {
            java.awt.Toolkit.getDefaultToolkit() != null
        } catch (_: Exception) {
            false
        }
    }

    override fun createMonitor(config: ClipboardConfig): ClipboardMonitor {
        return AwtOSClipboardMonitor(
            listener = config.listener,
            intervalMillis = config.pollingIntervalMs,
            enableDuplicateFiltering = config.enableDuplicateFiltering,
            errorHandler = config.errorHandler,
        )
    }
}

/**
 * Creates clipboard monitors for JVM desktop platforms.
 *
 * Automatically selects the appropriate implementation based on the operating system:
 * - **Windows**: Native Win32 clipboard notifications (event-driven, no polling)
 * - **macOS/Linux**: AWT Toolkit clipboard with polling
 *
 * All implementations support the full range of clipboard content types with
 * platform-specific optimizations.
 *
 * Example:
 * ```kotlin
 * val monitor = ClipboardMonitor.Builder()
 *     .setListener(listener)
 *     .setPollingInterval(250) // Used on macOS/Linux
 *     .build()
 * monitor.start()
 * ```
 *
 * **Windows Implementation:**
 * - Native clipboard change notifications
 * - Minimal CPU usage (event-driven)
 * - Supports text, HTML, RTF, files, and images
 *
 * **macOS/Linux Implementation:**
 * - Configurable polling (default: 200ms)
 * - Supports text, HTML, RTF, files, and images
 * - Background thread callbacks
 *
 * @see WindowsClipboardMonitor
 * @see AwtOSClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    internal actual val registry: PlatformRegistry = PlatformRegistry()

    init {
        registry.register(WindowsStrategy())
        registry.register(AwtStrategy())
    }

    /**
     * Creates a clipboard monitor with the specified configuration.
     *
     * Automatically detects the operating system and creates the appropriate
     * implementation.
     *
     * @param config The configuration for the monitor
     * @return A new JVM clipboard monitor
     * @throws UnsupportedOperationException if the OS is not supported or AWT is unavailable
     */
    public actual fun create(config: ClipboardConfig): ClipboardMonitor {
        val strategy = registry.selectStrategy()
            ?: throw UnsupportedOperationException(
                "No applicable clipboard strategy found for JVM platform. " +
                    "OS: ${Platform.getOSType()}, " +
                    "Supported: Windows, macOS, Linux with AWT",
            )

        return strategy.createMonitor(config)
    }

    /**
     * Creates a clipboard monitor with default configuration.
     *
     * @param listener The listener to receive clipboard change notifications
     * @return A new JVM clipboard monitor with default settings
     * @throws UnsupportedOperationException if the OS is not supported or AWT is unavailable
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return create(ClipboardConfig.default(listener))
    }
}

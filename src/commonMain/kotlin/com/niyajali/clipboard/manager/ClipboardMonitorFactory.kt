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
 * Creates clipboard monitors appropriate for the current platform.
 *
 * Automatically selects and creates the optimal clipboard monitor implementation
 * for the platform your application is running on (Android, Windows, macOS, iOS, etc.).
 *
 * **Supported Platforms:**
 * - Android: Requires initialization via [init] or AndroidX Startup
 * - Windows: Native clipboard notifications
 * - macOS/Linux: Polling-based monitoring
 * - iOS: UIPasteboard monitoring
 * - JavaScript: Browser Clipboard API
 *
 * **Basic Usage:**
 * ```kotlin
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start()
 * ```
 *
 * **Configured Usage (Recommended):**
 * ```kotlin
 * val monitor = ClipboardMonitor.Builder()
 *     .setListener(listener)
 *     .setDebounceDelay(100)
 *     .build()
 * monitor.start()
 * ```
 *
 * @see ClipboardMonitor
 * @see ClipboardMonitorBuilder
 * @see ClipboardConfig
 * @since 1.0.0
 */
public expect object ClipboardMonitorFactory {
    /**
     * Internal registry of platform implementations.
     *
     * Used internally to manage and select platform-specific monitor implementations.
     */
    internal val registry: PlatformRegistry

    /**
     * Creates a clipboard monitor with the specified configuration.
     *
     * Automatically selects the appropriate implementation for the current platform
     * and applies the provided configuration settings.
     *
     * Platform-specific behavior:
     * - Android: Uses ClipboardManager with event-driven monitoring
     * - Windows: Uses Win32 clipboard notifications
     * - macOS/Linux: Uses AWT Toolkit with polling
     * - iOS: Uses UIPasteboard with change detection
     * - JavaScript: Uses browser Clipboard API with polling
     *
     * Example:
     * ```kotlin
     * val config = ClipboardConfig(
     *     listener = myListener,
     *     debounceDelayMs = 100,
     *     pollingIntervalMs = 250
     * )
     * val monitor = ClipboardMonitorFactory.create(config)
     * ```
     *
     * @param config The configuration settings for the monitor
     * @return A new clipboard monitor ready to be started
     * @throws IllegalStateException on Android if not initialized
     * @throws UnsupportedOperationException if no suitable implementation is available
     */
    public fun create(config: ClipboardConfig): ClipboardMonitor

    /**
     * Creates a clipboard monitor with default configuration.
     *
     * Convenience method for simple use cases. Equivalent to:
     * ```kotlin
     * create(ClipboardConfig.default(listener))
     * ```
     *
     * For custom configuration, use [ClipboardMonitorBuilder] instead:
     * ```kotlin
     * ClipboardMonitor.Builder()
     *     .setListener(listener)
     *     .setDebounceDelay(100)
     *     .build()
     * ```
     *
     * @param listener The listener to receive clipboard change notifications
     * @return A new clipboard monitor with default settings
     * @throws IllegalStateException on Android if not initialized
     * @throws UnsupportedOperationException if no suitable implementation is available
     */
    public fun create(listener: ClipboardListener): ClipboardMonitor
}

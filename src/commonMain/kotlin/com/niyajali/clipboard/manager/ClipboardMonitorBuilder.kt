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
 * Creates configured clipboard monitors with customizable settings.
 *
 * Provides a convenient way to configure and create clipboard monitors.
 * Use method chaining to set desired options, then call [build] to create
 * the monitor.
 *
 * Example:
 * ```kotlin
 * val monitor = ClipboardMonitor.Builder()
 *     .setListener(myListener)
 *     .setDebounceDelay(100)
 *     .build()
 * ```
 *
 * Note: This class is not thread-safe. Create separate instances for different threads.
 *
 * @since 1.0.0
 */
public class ClipboardMonitorBuilder {
    private var listener: ClipboardListener? = null
    private var debounceDelayMs: Long = ClipboardConfig.DEFAULT_DEBOUNCE_DELAY_MS
    private var pollingIntervalMs: Long = ClipboardConfig.DEFAULT_POLLING_INTERVAL_MS
    private var enableDuplicateFiltering: Boolean =
        ClipboardConfig.DEFAULT_ENABLE_DUPLICATE_FILTERING
    private var errorHandler: ((Throwable) -> Unit)? = null

    /**
     * Sets the clipboard change listener.
     *
     * Required before calling [build].
     *
     * @param listener The listener to receive clipboard change notifications
     * @return This builder for method chaining
     */
    public fun setListener(listener: ClipboardListener): ClipboardMonitorBuilder = apply {
        this.listener = listener
    }

    /**
     * Sets the debounce delay in milliseconds.
     *
     * Debouncing delays notifications to group rapid clipboard changes together.
     * Useful on Android where copying formatted text may trigger multiple events.
     *
     * Platform usage:
     * - Android: Used for debouncing
     * - Windows: Used for duplicate filtering
     * - Other platforms: Ignored
     *
     * Valid range: 0-1000ms (enforced when building)
     *
     * @param delayMs Delay in milliseconds (0 = no debouncing)
     * @return This builder for method chaining
     */
    public fun setDebounceDelay(delayMs: Long): ClipboardMonitorBuilder = apply {
        this.debounceDelayMs = delayMs
    }

    /**
     * Sets the polling interval in milliseconds.
     *
     * Determines how often to check for clipboard changes on platforms without
     * native change notifications. Lower values provide faster updates but use
     * more CPU and battery.
     *
     * Platform usage:
     * - macOS/Linux: Used for clipboard polling
     * - iOS: Used for pasteboard polling
     * - JavaScript: Used for browser polling
     * - Windows/Android: Ignored (use native events)
     *
     * Valid range: 50-5000ms (enforced when building)
     *
     * Trade-offs:
     * - 50-100ms: Very responsive, higher CPU usage
     * - 200-500ms: Balanced (recommended)
     * - 1000-5000ms: Battery-efficient, slower response
     *
     * @param intervalMs Polling interval in milliseconds
     * @return This builder for method chaining
     */
    public fun setPollingInterval(intervalMs: Long): ClipboardMonitorBuilder = apply {
        this.pollingIntervalMs = intervalMs
    }

    /**
     * Enables or disables duplicate content filtering.
     *
     * When enabled, the monitor skips notifications when clipboard content
     * hasn't changed since the last notification. This reduces unnecessary
     * processing and callbacks.
     *
     * Disable only if your application needs every clipboard event, even
     * when the content is identical.
     *
     * @param enable true to enable filtering, false to receive all events
     * @return This builder for method chaining
     */
    public fun enableDuplicateFiltering(enable: Boolean): ClipboardMonitorBuilder = apply {
        this.enableDuplicateFiltering = enable
    }

    /**
     * Sets a custom error handler for monitoring errors.
     *
     * By default, errors during monitoring are silently ignored to prevent
     * the monitor from stopping. Provide a handler to log or react to errors.
     *
     * The handler may be called on platform-specific background threads,
     * so ensure it's thread-safe if accessing shared state.
     *
     * Common errors include:
     * - Permission denied (especially on JavaScript)
     * - Clipboard access failures
     * - Listener callback exceptions
     *
     * Example:
     * ```kotlin
     * .setErrorHandler { error ->
     *     Log.e("Clipboard", "Error: ${error.message}", error)
     * }
     * ```
     *
     * @param handler Callback invoked when errors occur, or null to disable
     * @return This builder for method chaining
     */
    public fun setErrorHandler(handler: ((Throwable) -> Unit)?): ClipboardMonitorBuilder = apply {
        this.errorHandler = handler
    }

    /**
     * Applies settings optimized for high-frequency monitoring.
     *
     * Suitable for applications requiring immediate clipboard updates
     * with minimal latency, such as clipboard history managers.
     *
     * Settings:
     * - Debounce delay: 0ms
     * - Polling interval: 100ms
     * - Duplicate filtering: enabled
     *
     * Trade-offs:
     * - Very responsive
     * - Higher CPU usage
     * - Increased battery drain on mobile
     *
     * @return This builder for method chaining
     */
    public fun useHighFrequencyPreset(): ClipboardMonitorBuilder = apply {
        debounceDelayMs = 0L
        pollingIntervalMs = 100L
        enableDuplicateFiltering = true
    }

    /**
     * Applies settings optimized for battery efficiency.
     *
     * Suitable for mobile devices or long-running applications where
     * power consumption is important.
     *
     * Settings:
     * - Debounce delay: 200ms
     * - Polling interval: 1000ms
     * - Duplicate filtering: enabled
     *
     * Trade-offs:
     * - Battery-efficient
     * - Lower CPU usage
     * - Slower response (up to 1 second delay)
     *
     * @return This builder for method chaining
     */
    public fun useLowPowerPreset(): ClipboardMonitorBuilder = apply {
        debounceDelayMs = 200L
        pollingIntervalMs = 1000L
        enableDuplicateFiltering = true
    }

    /**
     * Applies balanced settings suitable for most applications.
     *
     * Provides good responsiveness while maintaining reasonable resource usage.
     * These are the default settings.
     *
     * Settings:
     * - Debounce delay: 50ms
     * - Polling interval: 200ms
     * - Duplicate filtering: enabled
     *
     * @return This builder for method chaining
     */
    public fun useBalancedPreset(): ClipboardMonitorBuilder = apply {
        debounceDelayMs = ClipboardConfig.DEFAULT_DEBOUNCE_DELAY_MS
        pollingIntervalMs = ClipboardConfig.DEFAULT_POLLING_INTERVAL_MS
        enableDuplicateFiltering = ClipboardConfig.DEFAULT_ENABLE_DUPLICATE_FILTERING
    }

    /**
     * Builds the configuration without creating a monitor.
     *
     * Useful if you want to inspect or modify the configuration before
     * creating the monitor.
     *
     * @return An immutable configuration object
     * @throws IllegalStateException if [setListener] was not called
     * @throws IllegalArgumentException if any configuration value is invalid
     */
    public fun buildConfig(): ClipboardConfig {
        val listener = checkNotNull(listener) {
            "Listener is required. Call setListener() before build()."
        }

        return ClipboardConfig(
            listener = listener,
            debounceDelayMs = debounceDelayMs,
            pollingIntervalMs = pollingIntervalMs,
            enableDuplicateFiltering = enableDuplicateFiltering,
            errorHandler = errorHandler,
        )
    }

    /**
     * Builds and creates a configured clipboard monitor.
     *
     * Creates a monitor appropriate for the current platform with the
     * specified settings. The monitor is created in a stopped state;
     * call [ClipboardMonitor.start] to begin monitoring.
     *
     * Example:
     * ```kotlin
     * val monitor = ClipboardMonitor.Builder()
     *     .setListener(myListener)
     *     .setDebounceDelay(100)
     *     .build()
     *
     * monitor.start()
     * ```
     *
     * @return A configured clipboard monitor ready to be started
     * @throws IllegalStateException if [setListener] was not called
     * @throws IllegalArgumentException if any configuration value is invalid
     * @throws UnsupportedOperationException if the current platform is not supported
     */
    public fun build(): ClipboardMonitor {
        val config = buildConfig()
        return ClipboardMonitorFactory.create(config)
    }

    public companion object {
        /**
         * Creates a new builder instance.
         *
         * @return A new builder with default settings
         */
        public fun create(): ClipboardMonitorBuilder = ClipboardMonitorBuilder()
    }
}

/**
 * Creates a new builder for configuring clipboard monitors.
 *
 * Example:
 * ```kotlin
 * val monitor = ClipboardMonitor.Builder()
 *     .setListener(myListener)
 *     .build()
 * ```
 *
 * @return A new builder instance
 */
public fun ClipboardMonitor.Companion.Builder(): ClipboardMonitorBuilder =
    ClipboardMonitorBuilder.create()


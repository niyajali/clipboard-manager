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
 * Configuration settings for clipboard monitors.
 *
 * Contains all customizable settings that control clipboard monitoring behavior.
 * Instances are immutable and can be safely shared across threads.
 *
 * Different platforms use different settings:
 * - Android uses [debounceDelayMs] to coalesce rapid changes
 * - Windows uses [debounceDelayMs] for duplicate filtering
 * - macOS, Linux, iOS, and JavaScript use [pollingIntervalMs] to check for changes
 *
 * @property listener The callback that receives clipboard change notifications
 * @property debounceDelayMs Delay in milliseconds before notifying about clipboard changes (0-1000ms, default: 50ms)
 * @property pollingIntervalMs How often to check for clipboard changes in milliseconds (50-5000ms, default: 200ms)
 * @property enableDuplicateFiltering Whether to ignore consecutive identical clipboard content (default: true)
 * @property errorHandler Optional callback for handling internal errors during monitoring
 *
 * @since 1.0.0
 */
public data class ClipboardConfig(
    val listener: ClipboardListener,
    val debounceDelayMs: Long = DEFAULT_DEBOUNCE_DELAY_MS,
    val pollingIntervalMs: Long = DEFAULT_POLLING_INTERVAL_MS,
    val enableDuplicateFiltering: Boolean = DEFAULT_ENABLE_DUPLICATE_FILTERING,
    val errorHandler: ((Throwable) -> Unit)? = null,
) {
    init {
        require(debounceDelayMs in DEBOUNCE_RANGE) {
            "debounceDelayMs must be between ${DEBOUNCE_RANGE.first} and ${DEBOUNCE_RANGE.last}, got $debounceDelayMs"
        }
        require(pollingIntervalMs in POLLING_RANGE) {
            "pollingIntervalMs must be between ${POLLING_RANGE.first} and ${POLLING_RANGE.last}, got $pollingIntervalMs"
        }
    }

    /**
     * Creates a modified copy of this configuration.
     *
     * @param listener New listener, or null to keep current value
     * @param debounceDelayMs New debounce delay, or null to keep current value
     * @param pollingIntervalMs New polling interval, or null to keep current value
     * @param enableDuplicateFiltering New filtering flag, or null to keep current value
     * @param errorHandler New error handler, or null to keep current value
     * @return A new configuration with the specified changes
     */
    public fun copy(
        listener: ClipboardListener? = null,
        debounceDelayMs: Long? = null,
        pollingIntervalMs: Long? = null,
        enableDuplicateFiltering: Boolean? = null,
        errorHandler: ((Throwable) -> Unit)? = null,
    ): ClipboardConfig = ClipboardConfig(
        listener = listener ?: this.listener,
        debounceDelayMs = debounceDelayMs ?: this.debounceDelayMs,
        pollingIntervalMs = pollingIntervalMs ?: this.pollingIntervalMs,
        enableDuplicateFiltering = enableDuplicateFiltering ?: this.enableDuplicateFiltering,
        errorHandler = errorHandler ?: this.errorHandler,
    )

    public companion object {
        /**
         * Default debounce delay in milliseconds.
         *
         * Used on Android and Windows to coalesce rapid clipboard changes.
         */
        public const val DEFAULT_DEBOUNCE_DELAY_MS: Long = 50L

        /**
         * Default polling interval in milliseconds.
         *
         * Used on macOS, Linux, iOS, and JavaScript where native change
         * notifications are not available.
         */
        public const val DEFAULT_POLLING_INTERVAL_MS: Long = 200L

        /**
         * Default duplicate filtering behavior.
         *
         * When enabled, consecutive identical clipboard content is filtered
         * to reduce unnecessary callbacks.
         */
        public const val DEFAULT_ENABLE_DUPLICATE_FILTERING: Boolean = true

        private val DEBOUNCE_RANGE = 0L..1000L
        private val POLLING_RANGE = 50L..5000L

        /**
         * Creates a configuration with default settings.
         *
         * Uses standard values suitable for most applications:
         * - Debounce delay: 50ms
         * - Polling interval: 200ms
         * - Duplicate filtering: enabled
         *
         * @param listener The clipboard listener to use
         * @return A configuration with default settings
         */
        public fun default(listener: ClipboardListener): ClipboardConfig = ClipboardConfig(listener = listener)

        /**
         * Creates a configuration optimized for high-frequency monitoring.
         *
         * Suitable for applications requiring immediate clipboard updates,
         * such as clipboard history managers or productivity tools.
         *
         * Settings:
         * - Debounce delay: 0ms (no debouncing)
         * - Polling interval: 100ms (frequent checks)
         * - Duplicate filtering: enabled
         *
         * Note: Higher CPU and battery usage on platforms using polling.
         *
         * @param listener The clipboard listener to use
         * @return A configuration optimized for responsiveness
         */
        public fun highFrequency(listener: ClipboardListener): ClipboardConfig = ClipboardConfig(
            listener = listener,
            debounceDelayMs = 0L,
            pollingIntervalMs = 100L,
            enableDuplicateFiltering = true,
        )

        /**
         * Creates a configuration optimized for battery efficiency.
         *
         * Suitable for mobile applications or long-running processes where
         * power consumption is a concern.
         *
         * Settings:
         * - Debounce delay: 200ms (aggressive debouncing)
         * - Polling interval: 1000ms (infrequent checks)
         * - Duplicate filtering: enabled
         *
         * Note: Lower responsiveness in exchange for reduced power usage.
         *
         * @param listener The clipboard listener to use
         * @return A configuration optimized for battery life
         */
        public fun lowPower(listener: ClipboardListener): ClipboardConfig = ClipboardConfig(
            listener = listener,
            debounceDelayMs = 200L,
            pollingIntervalMs = 1000L,
            enableDuplicateFiltering = true,
        )
    }
}

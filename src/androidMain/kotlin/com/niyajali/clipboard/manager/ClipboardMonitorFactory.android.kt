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

import android.content.Context
import androidx.annotation.RestrictTo

/**
 * Android platform implementation for creating clipboard monitors.
 */
private class AndroidStrategy(private val context: Context) : PlatformStrategy {
    override val priority: Int = 100

    override fun isApplicable(): Boolean = true

    override fun createMonitor(config: ClipboardConfig): ClipboardMonitor {
        return AndroidClipboardMonitor(
            context = context,
            listener = config.listener,
            debounceMillis = config.debounceDelayMs,
            enableDuplicateFiltering = config.enableDuplicateFiltering,
            errorHandler = config.errorHandler,
        )
    }
}

/**
 * Creates clipboard monitors for Android.
 *
 * Uses Android's ClipboardManager API for efficient, event-driven clipboard monitoring.
 * Automatically registers itself via AndroidX Startup, so manual initialization is
 * typically not required.
 *
 * **Automatic Initialization:**
 * The library initializes automatically when your app starts. No manual setup needed.
 *
 * **Manual Initialization (if AndroidX Startup is disabled):**
 * ```kotlin
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         ClipboardMonitorFactory.init(this)
 *     }
 * }
 * ```
 *
 * **Implementation Details:**
 * - Uses OnPrimaryClipChangedListener for change notifications
 * - Callbacks delivered on the Android main thread
 * - Supports text, HTML, URIs, and image detection
 * - RTF format not supported on Android
 *
 * @see AndroidClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    @Volatile
    private var appContext: Context? = null

    internal actual val registry: PlatformRegistry = PlatformRegistry()

    /**
     * Initializes the factory with the application context.
     *
     * Called automatically by ClipboardInitializer when using AndroidX Startup.
     * Manual initialization only needed if you've disabled automatic initialization.
     *
     * Safe to call multiple times.
     *
     * @param context Any context (automatically converted to application context)
     */
    @JvmStatic
    public fun init(context: Context) {
        if (appContext == null) {
            synchronized(this) {
                if (appContext == null) {
                    appContext = context.applicationContext
                    registry.register(AndroidStrategy(appContext!!))
                }
            }
        }
    }

    /**
     * Returns the application context.
     */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    internal val context: Context
        get() = appContext
            ?: throw IllegalStateException(
                """
                ClipboardMonitorFactory is not initialized. 
                
                This usually happens when:
                1. AndroidX Startup is disabled in your manifest
                2. You're running in a test environment
                
                Solution: Call ClipboardMonitorFactory.init(context) before creating monitors.
                
                Example:
                class MyApplication : Application() {
                    override fun onCreate() {
                        super.onCreate()
                        ClipboardMonitorFactory.init(this)
                    }
                }
                """.trimIndent(),
            )

    /**
     * Checks if the factory has been initialized.
     *
     * @return true if [init] has been called
     */
    @JvmStatic
    public fun isInitialized(): Boolean = appContext != null

    /**
     * Creates a clipboard monitor with the specified configuration.
     *
     * The monitor uses Android's ClipboardManager and delivers callbacks
     * on the main thread.
     *
     * Example:
     * ```kotlin
     * val config = ClipboardConfig(
     *     listener = myListener,
     *     debounceDelayMs = 100
     * )
     * val monitor = ClipboardMonitorFactory.create(config)
     * monitor.start()
     * ```
     *
     * @param config The configuration for the monitor
     * @return A new Android clipboard monitor
     * @throws IllegalStateException if the factory has not been initialized
     */
    public actual fun create(config: ClipboardConfig): ClipboardMonitor {
        val strategy = registry.selectStrategy()
            ?: throw UnsupportedOperationException(
                "No applicable clipboard strategy found for Android platform. " +
                    "Ensure ClipboardMonitorFactory.init() has been called.",
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

    /**
     * Resets the factory (for testing).
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    internal fun reset() {
        appContext = null
        registry.clear()
    }
}

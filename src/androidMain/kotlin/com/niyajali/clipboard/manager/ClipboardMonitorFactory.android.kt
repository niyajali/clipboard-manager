package com.niyajali.clipboard.manager

import android.content.Context
import androidx.annotation.RestrictTo

/**
 * Android-specific implementation of [ClipboardMonitorFactory].
 *
 * This factory creates [AndroidClipboardMonitor] instances that use the Android
 * `ClipboardManager` API to monitor clipboard changes efficiently.
 *
 * **Initialization:**
 *
 * The factory requires a Context to be initialized before creating monitors.
 * This happens automatically via AndroidX Startup:
 *
 * ```kotlin
 * // No manual initialization needed!
 * // ClipboardInitializer handles this automatically
 *
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start()
 * ```
 *
 * **Manual Initialization (Optional):**
 *
 * If you need to initialize manually or aren't using AndroidX Startup:
 *
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
 * - Uses `ClipboardManager.OnPrimaryClipChangedListener` for efficient change detection
 * - Automatically debounces rapid clipboard changes (default: 50ms)
 * - All callbacks are delivered on the Android main thread (UI thread)
 * - Supports text, HTML, URIs, and image detection
 * - RTF format is not supported on Android (always null)
 *
 * @see AndroidClipboardMonitor
 * @see ClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    @Volatile
    private var appContext: Context? = null

    /**
     * Initializes the factory with the application context.
     *
     * **Note:** This method is called automatically by [ClipboardInitializer]
     * when using AndroidX Startup. Manual initialization is only needed if
     * you've disabled automatic initialization.
     *
     * This method is idempotent; calling it multiple times is safe.
     *
     * @param context Any context (will be converted to application context internally)
     *
     * @see ClipboardInitializer
     */
    @JvmStatic
    public fun init(context: Context) {
        if (appContext == null) {
            synchronized(this) {
                if (appContext == null) {
                    appContext = context.applicationContext
                }
            }
        }
    }

    /**
     * Internal getter for application context.
     *
     * @hide
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
                """.trimIndent()
            )

    /**
     * Checks if the factory has been initialized.
     *
     * @return true if [init] has been called, false otherwise
     */
    @JvmStatic
    public fun isInitialized(): Boolean = appContext != null

    /**
     * Creates a new [AndroidClipboardMonitor] instance.
     *
     * The monitor uses the Android `ClipboardManager` to track clipboard changes
     * and delivers callbacks on the main thread.
     *
     * **Usage Example:**
     * ```kotlin
     * val listener = object : ClipboardListener {
     *     override fun onClipboardChange(content: ClipboardContent) {
     *         // Called on main thread
     *         Log.d("Clipboard", "Text: ${content.text}")
     *     }
     * }
     *
     * val monitor = ClipboardMonitorFactory.create(listener)
     * monitor.start()
     * ```
     *
     * @param listener The listener that will receive clipboard change notifications
     *                 on the Android main thread.
     *
     * @return A new [AndroidClipboardMonitor] instance in a stopped state.
     *
     * @throws IllegalStateException if the factory has not been initialized
     *
     * @see AndroidClipboardMonitor
     * @see init
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return AndroidClipboardMonitor(context, listener)
    }

    /**
     * Resets the factory (for testing purposes).
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    internal fun reset() {
        appContext = null
    }
}

package com.niyajali.clipboard.manager

import android.content.Context
import androidx.startup.Initializer

/**
 * AndroidX Startup initializer for automatic [ClipboardMonitorFactory] initialization.
 *
 * This class uses the AndroidX Startup library to automatically initialize the
 * clipboard manager when your application starts, eliminating the need for manual
 * initialization in most cases.
 *
 * **Automatic Initialization:**
 *
 * If you have AndroidX Startup in your dependencies, initialization happens automatically:
 *
 * ```kotlin
 * // No code needed! Just start using the library:
 * val monitor = ClipboardMonitorFactory.create(listener)
 * ```
 *
 * **Disabling Automatic Initialization:**
 *
 * To disable automatic initialization and use manual initialization instead,
 * add this to your AndroidManifest.xml:
 *
 * ```xml
 * <provider
 *     android:name="androidx.startup.InitializationProvider"
 *     android:authorities="${applicationId}.androidx-startup"
 *     tools:node="remove" />
 * ```
 *
 * Then manually initialize in your Application class:
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
 * **Performance:**
 *
 * This initializer is extremely lightweight and runs synchronously during app startup.
 * It only stores the application context reference and performs no heavy operations.
 *
 * @see ClipboardMonitorFactory.init
 * @see <a href="https://developer.android.com/topic/libraries/app-startup">AndroidX Startup</a>
 * @since 1.0.0
 */
public class ClipboardInitializer : Initializer<Unit> {
    /**
     * Initializes the [ClipboardMonitorFactory] with the application context.
     *
     * This method is called automatically by AndroidX Startup when the application starts.
     *
     * @param context The application context provided by the Startup library
     * @return Unit (no return value needed)
     */
    override fun create(context: Context) {
        ClipboardMonitorFactory.init(context)
    }

    /**
     * Returns the list of initializers that this initializer depends on.
     *
     * The clipboard manager has no dependencies, so this returns an empty list.
     *
     * @return An empty list
     */
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

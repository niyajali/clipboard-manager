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

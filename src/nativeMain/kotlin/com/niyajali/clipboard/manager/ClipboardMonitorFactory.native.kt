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
 * iOS/Native-specific implementation of [ClipboardMonitorFactory].
 *
 * This factory creates clipboard monitors for iOS devices using UIPasteboard.
 * Currently supports iOS targets; other native targets are not yet implemented.
 *
 * **Supported Platforms:**
 * - iOS (iosArm64, iosX64, iosSimulatorArm64)
 * - Other native platforms: Not yet supported
 *
 * **Usage Example (iOS):**
 * ```kotlin
 * val listener = object : ClipboardListener {
 *     override fun onClipboardChange(content: ClipboardContent) {
 *         println("iOS Clipboard: ${content.text}")
 *     }
 * }
 *
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start()
 * ```
 *
 * **iOS Permissions:**
 * iOS 14+ will show a paste notification banner when the app first accesses
 * the pasteboard. No explicit permission prompt is required.
 *
 * @see IOSClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    /**
     * Creates a new clipboard monitor for the current native platform.
     *
     * **iOS:** Returns an [IOSClipboardMonitor] using UIPasteboard
     * **Other:** Throws [UnsupportedOperationException]
     *
     * @param listener The listener that will receive clipboard change notifications
     *
     * @return A new clipboard monitor instance in a stopped state
     *
     * @throws UnsupportedOperationException on non-iOS native platforms
     *
     * @see IOSClipboardMonitor
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor = IOSClipboardMonitor(listener)
}

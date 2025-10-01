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
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return IOSClipboardMonitor(listener)
    }
}

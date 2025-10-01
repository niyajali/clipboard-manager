package com.niyajali.clipboard.manager

/**
 * Factory for creating platform-specific [ClipboardMonitor] instances.
 *
 * This expect/actual object provides a unified API for creating clipboard monitors
 * across all supported platforms. The factory automatically selects the appropriate
 * implementation based on the target platform.
 *
 * **Supported Platforms:**
 * - **Android**: Requires initialization via [init] (or automatic via AndroidX Startup)
 * - **JVM**: Automatically detects Windows, macOS, or Linux and uses appropriate implementation
 * - **iOS**: Native implementation using UIPasteboard
 * - **JS/WasmJS**: Browser-based implementation using Clipboard API
 *
 * **Android Initialization:**
 * ```kotlin
 * // Option 1: Manual initialization (in Application.onCreate)
 * ClipboardMonitorFactory.init(applicationContext)
 *
 * // Option 2: Automatic initialization (recommended)
 * // Add androidx.startup dependency and ClipboardInitializer will auto-initialize
 * ```
 *
 * **Usage Example:**
 * ```kotlin
 * val listener = object : ClipboardListener {
 *     override fun onClipboardChange(content: ClipboardContent) {
 *         println("Clipboard changed: ${content.text}")
 *     }
 * }
 *
 * val monitor = ClipboardMonitorFactory.create(listener)
 * monitor.start()
 * // ... monitor is now active ...
 * monitor.stop()
 * ```
 *
 * @see ClipboardMonitor
 * @see ClipboardListener
 * @since 1.0.0
 */
public expect object ClipboardMonitorFactory {
    /**
     * Creates a new [ClipboardMonitor] instance for the current platform.
     *
     * The returned monitor is platform-specific but conforms to the common
     * [ClipboardMonitor] interface. The monitor is created in a stopped state;
     * call [ClipboardMonitor.start] to begin monitoring.
     *
     * **Platform-Specific Implementations:**
     * - **Android**: [AndroidClipboardMonitor] - requires context initialization
     * - **Windows**: [WindowsClipboardMonitor] - uses Win32 clipboard notifications
     * - **macOS/Linux**: [AwtOSClipboardMonitor] - uses AWT clipboard polling
     * - **iOS**: [IOSClipboardMonitor] - uses UIPasteboard change notifications
     * - **JS/WasmJS**: [JSClipboardMonitor] - uses browser Clipboard API
     *
     * @param listener The listener that will receive clipboard change notifications.
     *                 Must not be null.
     *
     * @return A new [ClipboardMonitor] instance ready to be started.
     *
     * @throws IllegalStateException on Android if [init] was not called first
     * @throws UnsupportedOperationException on unsupported platforms
     *
     * @see ClipboardMonitor.start
     * @see ClipboardListener
     */
    public fun create(listener: ClipboardListener): ClipboardMonitor
}

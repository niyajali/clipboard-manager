package com.niyajali.clipboard.manager

import com.niyajali.clipboard.manager.windows.WindowsClipboardMonitor
import com.sun.jna.Platform

/**
 * JVM-specific implementation of [ClipboardMonitorFactory].
 *
 * This factory automatically detects the operating system and creates the
 * appropriate clipboard monitor implementation:
 * - **Windows**: Native Win32 clipboard notifications via message-only window
 * - **macOS/Linux**: AWT-based clipboard polling
 *
 * All implementations support the full [ClipboardMonitor] interface with
 * platform-specific optimizations.
 *
 * **Platform Detection:**
 * The factory uses JNA's `Platform` class to detect the OS and select the
 * best implementation automatically.
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
 *
 * // Monitor runs in background...
 *
 * monitor.stop() // Clean up when done
 * ```
 *
 * **Windows Implementation:**
 * - Uses `AddClipboardFormatListener` for native change notifications
 * - Minimal CPU usage (event-driven, no polling)
 * - Supports text, HTML, RTF, files, and images
 *
 * **macOS/Linux Implementation:**
 * - Uses AWT Toolkit clipboard with periodic polling (200ms default)
 * - Supports text, HTML, RTF, files, and images
 * - Configurable polling interval
 *
 * **Thread Safety:**
 * All monitor implementations are thread-safe. Callbacks may be invoked on
 * platform-specific background threads.
 *
 * **Resource Management:**
 * Always call [ClipboardMonitor.stop] when done monitoring to release resources:
 * - Windows: Destroys message window and unregisters clipboard listener
 * - macOS/Linux: Shuts down scheduled executor thread
 *
 * @see WindowsClipboardMonitor
 * @see AwtOSClipboardMonitor
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    /**
     * Creates a new platform-specific [ClipboardMonitor] for the current OS.
     *
     * The factory automatically detects Windows, macOS, or Linux and returns
     * the appropriate implementation.
     *
     * **Implementation Selection:**
     * - `Platform.isWindows()` → [WindowsClipboardMonitor]
     * - `Platform.isMac() || Platform.isLinux()` → [AwtOSClipboardMonitor]
     * - Other → throws [IllegalStateException]
     *
     * @param listener The listener that will receive clipboard change notifications.
     *                 Callbacks may be invoked on background threads.
     *
     * @return A new platform-specific [ClipboardMonitor] in a stopped state.
     *
     * @throws IllegalStateException if the current OS is not supported
     *
     * @see WindowsClipboardMonitor
     * @see AwtOSClipboardMonitor
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return when {
            Platform.isWindows() -> WindowsClipboardMonitor(listener)
            Platform.isMac() || Platform.isLinux() -> AwtOSClipboardMonitor(listener)
            else -> error("Unsupported OS type: ${Platform.getOSType()}")
        }
    }
}

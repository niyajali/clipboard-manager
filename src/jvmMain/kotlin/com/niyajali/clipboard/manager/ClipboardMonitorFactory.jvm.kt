package com.niyajali.clipboard.manager

import com.niyajali.clipboard.manager.windows.WindowsClipboardMonitor
import com.sun.jna.Platform

public actual object ClipboardMonitorFactory {
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return when {
            Platform.isWindows() -> WindowsClipboardMonitor(listener)
            Platform.isMac() || Platform.isLinux() -> AwtOSClipboardMonitor(listener)
            else -> error("Unsupported OS type: ${Platform.getOSType()}")
        }
    }
}
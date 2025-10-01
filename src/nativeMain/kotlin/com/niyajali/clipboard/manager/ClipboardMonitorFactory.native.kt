package com.niyajali.clipboard.manager

public actual object ClipboardMonitorFactory {
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        error("ClipboardMonitor not supported on native platforms")
    }
}
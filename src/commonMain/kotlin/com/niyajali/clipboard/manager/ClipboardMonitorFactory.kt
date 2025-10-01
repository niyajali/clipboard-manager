package com.niyajali.clipboard.manager

public expect object ClipboardMonitorFactory {
    public fun create(listener: ClipboardListener): ClipboardMonitor
}
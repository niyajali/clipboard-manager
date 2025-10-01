package com.niyajali.clipboard.manager

public interface ClipboardMonitor {
    public fun start()
    public fun stop()
    public fun isRunning(): Boolean
    public fun getCurrentContent(): ClipboardContent
}
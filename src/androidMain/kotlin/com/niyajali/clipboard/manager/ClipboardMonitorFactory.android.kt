package com.niyajali.clipboard.manager

import android.content.Context

public actual object ClipboardMonitorFactory {
    private lateinit var appContext: Context

    public fun init(context: Context) {
        appContext = context.applicationContext
    }

    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        check(::appContext.isInitialized) {
            "ClipboardMonitorFactory.init(context) must be called before create(listener)."
        }
        return AndroidClipboardMonitor(appContext, listener)
    }
}
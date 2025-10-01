package com.niyajali.clipboard.manager

import kotlinx.datetime.Clock

public class StubClipboardMonitor : ClipboardMonitor {
    override fun start() {
    }

    override fun stop() {
    }

    override fun isRunning(): Boolean {
        return false
    }

    override fun getCurrentContent(): ClipboardContent {
        return ClipboardContent(timestamp = Clock.System.now().toEpochMilliseconds())
    }
}
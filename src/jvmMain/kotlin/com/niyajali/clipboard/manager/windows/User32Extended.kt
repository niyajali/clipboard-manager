package com.niyajali.clipboard.manager.windows

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.W32APIOptions

internal interface User32Extended : User32 {
    fun addClipboardFormatListener(hWnd: WinDef.HWND): Boolean
    fun removeClipboardFormatListener(hWnd: WinDef.HWND): Boolean
    fun getClipboardSequenceNumber(): DWORD

    companion object {
        // W32APIOptions.DEFAULT_OPTIONS sets Unicode + useLastError=true,
        // so Kernel32.GetLastError() will return the right code.
        val INSTANCE: User32Extended =
            Native.load(
                "user32",
                User32Extended::class.java,
                W32APIOptions.DEFAULT_OPTIONS
            )
    }
}
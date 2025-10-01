/**
 * Copyright 2025 Sk Niyaj Ali
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.niyajali.clipboard.manager

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIPasteboard

/**
 * iOS implementation of [ClipboardMonitor] using UIPasteboard.
 *
 * This implementation uses `UIPasteboard.general` to monitor clipboard changes
 * on iOS devices. It employs polling of the pasteboard's change count to detect
 * when new content is copied.
 *
 * **iOS Specifics:**
 * - Uses `UIPasteboard.generalPasteboard` for system-wide clipboard access
 * - Monitors `changeCount` property for efficient change detection
 * - Supports text, HTML, URLs, and image detection
 * - RTF support depends on pasteboard content
 * - Configurable duplicate filtering
 * - Optional error handling
 *
 * **Permissions:**
 * - iOS 14+: Shows paste notification banner on first access
 * - No explicit permission required, but user is notified
 *
 * **Threading:**
 * - All callbacks are delivered on the main thread via NSTimer
 * - Safe to update UI directly from the callback
 * - iOS uses single-threaded main run loop, so no complex synchronization needed
 *
 * **Note on Beta API Usage:**
 * This implementation uses Kotlin/Native's `@BetaInteropApi` for Foundation framework
 * interoperability. This API is stable in practice but marked as beta by JetBrains.
 * The usage here is limited to standard Foundation types (NSString, NSDate) and should
 * remain compatible across Kotlin versions.
 *
 * @param listener The listener to receive clipboard change notifications
 * @param pollingIntervalMs Polling interval in seconds (default: 0.5 seconds)
 * @param enableDuplicateFiltering Enable duplicate content filtering (default: true)
 * @param errorHandler Optional callback for internal errors
 *
 * @see ClipboardMonitor
 * @since 1.0.0
 */
internal class IOSClipboardMonitor(
    private val listener: ClipboardListener,
    private val pollingIntervalMs: Double = 0.5,
    private val enableDuplicateFiltering: Boolean = true,
    private val errorHandler: ((Throwable) -> Unit)? = null,
) : ClipboardMonitor {

    // iOS runs on main thread, no need for AtomicReference
    private var running = false
    private var timer: NSTimer? = null
    private var lastChangeCount: Long = 0
    private val pasteboard = UIPasteboard.generalPasteboard

    override fun start() {
        if (running) return
        running = true

        lastChangeCount = pasteboard.changeCount

        // Schedule timer on main run loop
        timer = NSTimer.scheduledTimerWithTimeInterval(
            interval = pollingIntervalMs,
            repeats = true,
        ) {
            checkPasteboard()
        }

        // Fire initial check
        checkPasteboard()
    }

    override fun stop() {
        if (!running) return
        running = false

        timer?.invalidate()
        timer = null
        lastChangeCount = 0
    }

    override fun isRunning(): Boolean = running

    override fun getCurrentContent(): ClipboardContent = readPasteboard()

    private fun checkPasteboard() {
        try {
            val currentChangeCount = pasteboard.changeCount
            
            // Only proceed if change count indicates new content
            if (enableDuplicateFiltering && currentChangeCount == lastChangeCount) {
                return
            }
            
            lastChangeCount = currentChangeCount
            val content = readPasteboard()
            
            try {
                listener.onClipboardChange(content)
            } catch (e: Throwable) {
                // Listener exceptions must not stop monitoring
                errorHandler?.invoke(e)
            }
        } catch (e: Throwable) {
            errorHandler?.invoke(e)
        }
    }

    /**
     * Reads the current pasteboard content.
     *
     * Note: Uses [@BetaInteropApi] for NSString creation from NSData. This is a stable
     * usage pattern that has been supported since early Kotlin/Native versions and is
     * unlikely to change in future releases.
     */
    @OptIn(BetaInteropApi::class)
    private fun readPasteboard(): ClipboardContent {
        val timestamp = NSDate().timeIntervalSince1970.toLong() * 1000

        val text = pasteboard.string

        val html = pasteboard.dataForPasteboardType("public.html")?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }

        val rtf = pasteboard.dataForPasteboardType("public.rtf")?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }

        val imageAvailable = pasteboard.hasImages

        val urls = pasteboard.URLs?.mapNotNull { urlObj ->
            (urlObj as? NSURL)?.path
        }

        return ClipboardContent(
            text = text,
            html = html,
            rtf = rtf,
            files = urls?.takeIf { it.isNotEmpty() },
            imageAvailable = imageAvailable,
            timestamp = timestamp,
        )
    }
}

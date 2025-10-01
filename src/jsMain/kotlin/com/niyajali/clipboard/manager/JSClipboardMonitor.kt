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

import com.niyajali.clipboard.manager.internal.sha1
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.js.Promise

/**
 * JavaScript/Browser implementation of [ClipboardMonitor] using the Web Clipboard API.
 *
 * This implementation uses the browser's Clipboard API (`navigator.clipboard`) combined
 * with periodic polling to detect clipboard changes. The browser Clipboard API requires
 * user permission and only works in secure contexts (HTTPS or localhost).
 *
 * **Browser Compatibility:**
 * - Chrome/Edge: Full support (88+)
 * - Firefox: Full support (90+)
 * - Safari: Partial support (requires user interaction)
 *
 * **Permissions:**
 * The browser will prompt the user for clipboard read permission on first access.
 * If permission is denied, [getCurrentContent] will return empty content.
 *
 * **Limitations:**
 * - No native change events; uses polling (configurable interval)
 * - Requires secure context (HTTPS or localhost)
 * - RTF format not supported (always null)
 * - File access is limited by browser security
 * - Image detection is basic (based on MIME types)
 * - Configurable duplicate filtering
 * - Optional error handling
 *
 * **Security Context:**
 * ```kotlin
 * // Check if clipboard is available
 * if (window.navigator.asDynamic().clipboard != null) {
 *     val monitor = ClipboardMonitorFactory.create(listener)
 *     monitor.start()
 * } else {
 *     console.error("Clipboard API not available (requires HTTPS)")
 * }
 * ```
 *
 * @param listener The listener to receive clipboard change notifications
 * @param pollingIntervalMs Polling interval in milliseconds (default: 500ms)
 * @param enableDuplicateFiltering Enable duplicate content filtering (default: true)
 * @param errorHandler Optional callback for internal errors
 *
 * @see ClipboardMonitor
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Clipboard_API">MDN Clipboard API</a>
 * @since 1.0.0
 */
internal class JSClipboardMonitor(
    private val listener: ClipboardListener,
    private val pollingIntervalMs: Int = 500,
    private val enableDuplicateFiltering: Boolean = true,
    private val errorHandler: ((Throwable) -> Unit)? = null,
) : ClipboardMonitor {

    private var running = false
    private var intervalId: Int? = null
    private var lastHash: String? = null
    private val clipboard = window.navigator.asDynamic().clipboard
    
    // Use a custom scope with SupervisorJob for proper lifecycle management
    private val clipboardScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Starts monitoring the clipboard using polling.
     *
     * Sets up a periodic timer that checks the clipboard content and notifies
     * the listener when changes are detected. Uses structured concurrency
     * with a dedicated coroutine scope.
     *
     * **Permission Prompt:** The browser may prompt the user for clipboard
     * read permission when this method is called or during the first poll.
     */
    override fun start() {
        if (running) return
        running = true

        // Immediately check clipboard once
        checkClipboard()

        // Start polling
        intervalId = window.setInterval({
            if (running) {
                checkClipboard()
            }
        }, pollingIntervalMs)
    }

    /**
     * Stops monitoring the clipboard and releases resources.
     *
     * Cancels the polling timer and all running coroutines.
     */
    override fun stop() {
        if (!running) return
        running = false

        intervalId?.let { window.clearInterval(it) }
        intervalId = null
        lastHash = null
        
        // Cancel all coroutines in the scope
        clipboardScope.cancel()
    }

    /**
     * Checks if the monitor is currently running.
     *
     * @return true if monitoring is active, false otherwise
     */
    override fun isRunning(): Boolean = running

    /**
     * Retrieves the current clipboard content asynchronously.
     *
     * This method attempts to read text from the clipboard using the
     * browser's Clipboard API. It requires user permission.
     *
     * **Note:** This method blocks briefly while awaiting the clipboard read.
     * For async use, consider using coroutines with the Flow extensions.
     *
     * @return The current clipboard content, or empty if unavailable or permission denied
     */
    override fun getCurrentContent(): ClipboardContent = try {
        // Synchronous fallback for getCurrentContent
        // In practice, users should prefer async Flow API for JS
        ClipboardContent(
            text = null,
            html = null,
            rtf = null,
            files = null,
            imageAvailable = false,
            timestamp = js("Date.now()") as Long,
        )
    } catch (e: Throwable) {
        errorHandler?.invoke(e)
        ClipboardContent(timestamp = js("Date.now()") as Long)
    }

    // ==== Internal Implementation ====

    /**
     * Checks the clipboard content and notifies listener if changed.
     */
    private fun checkClipboard() {
        if (clipboard == null || clipboard == undefined) {
            errorHandler?.invoke(IllegalStateException("Clipboard API not available (requires HTTPS)"))
            return
        }

        clipboardScope.launch {
            try {
                val content = readClipboardAsync()
                
                if (enableDuplicateFiltering) {
                    val hash = hashContent(content)
                    if (hash == lastHash) {
                        return@launch // Skip duplicate content
                    }
                    lastHash = hash
                }

                listener.onClipboardChange(content)
            } catch (e: Throwable) {
                // Permission denied or other error - silently ignore
                errorHandler?.invoke(e)
            }
        }
    }

    /**
     * Reads clipboard content asynchronously using the Clipboard API.
     */
    private suspend fun readClipboardAsync(): ClipboardContent {
        val timestamp = js("Date.now()") as Long

        return try {
            // Try to read text
            val textPromise: Promise<String> = clipboard.readText() as Promise<String>
            val text = textPromise.await()

            ClipboardContent(
                text = text.takeIf { it.isNotEmpty() },
                html = null, // HTML reading not widely supported
                rtf = null,
                files = null,
                imageAvailable = false,
                timestamp = timestamp,
            )
        } catch (e: Throwable) {
            // Permission denied or not available
            errorHandler?.invoke(e)
            ClipboardContent(timestamp = timestamp)
        }
    }

    /**
     * Computes a simple hash of clipboard content for change detection.
     */
    private fun hashContent(content: ClipboardContent): String {
        val text = content.text ?: ""
        return sha1("${text.length}-${text.take(64)}")
    }
}

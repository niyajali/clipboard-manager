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

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android implementation of [ClipboardMonitor] using the Android ClipboardManager API.
 *
 * This implementation uses `ClipboardManager.OnPrimaryClipChangedListener` to receive
 * clipboard change notifications from the Android system. Changes are automatically
 * debounced to prevent duplicate notifications during rapid clipboard updates.
 *
 * **Features:**
 * - Efficient event-driven monitoring (no polling)
 * - Automatic debouncing (default: 50ms) to coalesce rapid changes
 * - Duplicate detection using content signatures
 * - Main thread callbacks (safe for UI updates)
 * - Support for text, HTML, URIs, and image detection
 *
 * **Supported Content Types:**
 * - **Text**: Plain text content via `ClipData.Item.coerceToText()`
 * - **HTML**: HTML formatted text via `ClipData.Item.htmlText`
 * - **URIs**: Content URIs for files, images, and other resources
 * - **Images**: Detection of image content via MIME types
 * - **RTF**: Not supported on Android (always null)
 *
 * **Threading:**
 * - The [ClipboardListener] is always invoked on the Android main thread
 * - Safe to update UI directly from the callback
 * - No need for additional thread synchronization
 *
 * **Content URI Handling:**
 * Files are represented as Android content URIs (e.g., `content://...`).
 * To access the actual files, use Android's ContentResolver:
 *
 * ```kotlin
 * content.files?.forEach { uriString ->
 *     val uri = Uri.parse(uriString)
 *     val inputStream = contentResolver.openInputStream(uri)
 *     // Process the file...
 * }
 * ```
 *
 * **Performance:**
 * - Minimal overhead; uses Android's built-in clipboard notifications
 * - Debouncing prevents excessive callback invocations
 * - Lightweight SHA-1 signatures for duplicate detection
 *
 * @param context The application context (provided by [ClipboardMonitorFactory])
 * @param listener The listener to receive clipboard change notifications
 * @param debounceMillis Debounce delay in milliseconds (default: 50ms)
 *
 * @see ClipboardMonitor
 * @see ClipboardMonitorFactory
 * @since 1.0.0
 */
internal class AndroidClipboardMonitor(
    private val context: Context,
    private val listener: ClipboardListener,
    private val debounceMillis: Long = 50L,
) : ClipboardMonitor {

    private val running = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastSignature: String? = null

    private val clipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val callback = ClipboardManager.OnPrimaryClipChangedListener {
        // Debounce: Remove any pending notifications and schedule a new one
        mainHandler.removeCallbacks(notifyRunnable)
        mainHandler.postDelayed(notifyRunnable, debounceMillis)
    }

    private val notifyRunnable = Runnable {
        if (!running.get()) return@Runnable

        val content = readClipboard()
        val signature = signatureOf(content)

        // Only notify if content has actually changed (prevent duplicates)
        if (signature != lastSignature) {
            lastSignature = signature
            try {
                listener.onClipboardChange(content)
            } catch (e: Throwable) {
                // Listener exceptions must not stop monitoring
                // Silently catch to maintain robustness
            }
        }
    }

    /**
     * Starts monitoring the clipboard for changes.
     *
     * Registers the clipboard change listener and immediately reads and reports
     * the current clipboard content.
     *
     * **Thread Safety:** This method is thread-safe and can be called from any thread.
     * The registration happens on the main thread.
     *
     * @throws IllegalStateException This implementation does not throw; failures are handled gracefully
     */
    override fun start() {
        if (running.getAndSet(true)) return

        // Register listener and fire initial snapshot (both on main thread)
        mainHandler.post {
            runCatching { clipboardManager.addPrimaryClipChangedListener(callback) }
            notifyRunnable.run()
        }
    }

    /**
     * Stops monitoring the clipboard and releases resources.
     *
     * Unregisters the clipboard change listener and cancels any pending notifications.
     *
     * **Thread Safety:** This method is thread-safe and can be called from any thread.
     */
    override fun stop() {
        if (!running.getAndSet(false)) return

        mainHandler.post {
            runCatching { clipboardManager.removePrimaryClipChangedListener(callback) }
        }
        mainHandler.removeCallbacksAndMessages(null)
        lastSignature = null
    }

    /**
     * Checks if the monitor is currently running.
     *
     * @return true if monitoring is active, false otherwise
     */
    override fun isRunning(): Boolean = running.get()

    /**
     * Retrieves the current clipboard content synchronously.
     *
     * This method reads the clipboard state directly without waiting for changes.
     * It can be called whether the monitor is running or not.
     *
     * @return The current clipboard content, or an empty [ClipboardContent] if clipboard is empty
     */
    override fun getCurrentContent(): ClipboardContent = readClipboard()

    // ==== Internal Implementation ====

    /**
     * Reads and parses the current clipboard content.
     *
     * Extracts text, HTML, URIs, and detects images from the Android ClipData.
     *
     * @return A [ClipboardContent] representing the current clipboard state
     */
    private fun readClipboard(): ClipboardContent {
        val clip: ClipData? = runCatching { clipboardManager.primaryClip }.getOrNull()
        val description: ClipDescription? = runCatching { clipboardManager.primaryClipDescription }.getOrNull()

        if (clip == null || clip.itemCount == 0) {
            return ClipboardContent(timestamp = System.currentTimeMillis())
        }

        var text: String? = null
        var html: String? = null
        val files: MutableList<String> = mutableListOf()
        var imageAvailable = false

        for (i in 0 until clip.itemCount) {
            val item = clip.getItemAt(i)

            // Extract text (covers plain text and coercible formats)
            val coerced = item.coerceToText(context)
            if (!coerced.isNullOrEmpty()) {
                text = if (text == null) {
                    coerced.toString()
                } else {
                    "$text\n$coerced"
                }
            }

            // Extract HTML if explicitly provided
            val htmlText = item.htmlText
            if (!htmlText.isNullOrEmpty()) {
                html = if (html == null) {
                    htmlText
                } else {
                    "$html\n$htmlText"
                }
            }

            // Extract URIs (could be files, images, or other content)
            val uri: Uri? = item.uri
            if (uri != null) {
                val mimeType = runCatching {
                    context.contentResolver.getType(uri)
                }.getOrNull()

                if (mimeType?.startsWith("image/") == true) {
                    imageAvailable = true
                }

                // Add URI as a "file-like" entry
                files.add(uri.toString())
            }

            // Note: Intents are ignored (non-standard clipboard content)
        }

        // Fallback: Check MIME types in description for image detection
        if (!imageAvailable && description != null) {
            for (i in 0 until description.mimeTypeCount) {
                val mime = description.getMimeType(i)
                if (mime.startsWith("image/") || mime == ClipDescription.MIMETYPE_TEXT_URILIST) {
                    imageAvailable = true
                    break
                }
            }
        }

        return ClipboardContent(
            text = text,
            html = html,
            rtf = null, // RTF is not a standard Android clipboard format
            files = files.takeIf { it.isNotEmpty() },
            imageAvailable = imageAvailable,
            timestamp = System.currentTimeMillis(),
        )
    }

    /**
     * Generates a lightweight signature for clipboard content.
     *
     * Uses SHA-1 hashing of content metadata (lengths and prefixes) to detect
     * duplicate content without comparing full payloads.
     *
     * @param content The clipboard content to sign
     * @return A SHA-1 hash string representing the content
     */
    private fun signatureOf(content: ClipboardContent): String {
        val sb = StringBuilder(128)

        fun add(name: String, value: String?) {
            if (value != null) {
                sb.append(name).append('#').append(value.length).append(';')
                sb.append(value.take(64)).append('|')
            } else {
                sb.append(name).append("#0;|")
            }
        }

        add("t", content.text)
        add("h", content.html)
        add("r", content.rtf)
        sb.append("i#").append(if (content.imageAvailable) 1 else 0).append('|')

        val fileList = content.files
        if (fileList != null) {
            sb.append("f#").append(fileList.size).append('|')
            fileList.take(8).forEach { sb.append(it).append('|') }
        } else {
            sb.append("f#0|")
        }

        return sha1(sb.toString())
    }

    /**
     * Computes SHA-1 hash of a string.
     *
     * @param input The string to hash
     * @return Hexadecimal representation of the SHA-1 hash
     */
    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        val hex = CharArray(bytes.size * 2)
        val hexChars = "0123456789abcdef".toCharArray()
        var index = 0
        for (byte in bytes) {
            val value = byte.toInt() and 0xFF
            hex[index++] = hexChars[value ushr 4]
            hex[index++] = hexChars[value and 0x0F]
        }
        return String(hex)
    }
}

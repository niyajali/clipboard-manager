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
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class AwtOSClipboardMonitor(
    private val listener: ClipboardListener,
    private val intervalMillis: Long = 200L,
    private val enableDuplicateFiltering: Boolean = true,
    private val errorHandler: ((Throwable) -> Unit)? = null,
) : ClipboardMonitor {

    private val running = AtomicBoolean(false)
    private val started = CountDownLatch(1)
    private var scheduler: ScheduledExecutorService? = null
    private var lastSignature: String? = null
    private var shutdownHook: Thread? = null

    override fun start() {
        if (running.get()) return
        running.set(true)

        scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "awt-ClipboardMonitor").apply { isDaemon = true }
        }.also { exec ->
            // Fire a first read quickly, then repeat.
            exec.scheduleAtFixedRate(::tickSafe, 0L, intervalMillis.coerceAtLeast(50L), TimeUnit.MILLISECONDS)
        }

        // Register a shutdown hook to ensure background executor is stopped
        shutdownHook = Thread { runCatching { stop() } }.also {
            runCatching { Runtime.getRuntime().addShutdownHook(it) }
        }

        started.countDown()
        started.await()
    }

    override fun stop() {
        if (!running.get()) return
        running.set(false)
        // Try to remove shutdown hook if we're not already in shutdown
        shutdownHook?.let { hook ->
            runCatching { Runtime.getRuntime().removeShutdownHook(hook) }
        }
        shutdownHook = null
        scheduler?.shutdownNow()
        scheduler = null
        lastSignature = null
    }

    override fun isRunning(): Boolean = running.get()

    override fun getCurrentContent(): ClipboardContent = readClipboard()

    // === Internals ===

    private fun tickSafe() {
        if (!running.get()) return
        try {
            val content = readClipboard()

            // Check for duplicates if filtering is enabled
            if (enableDuplicateFiltering) {
                val sig = signatureOf(content)
                if (sig == lastSignature) {
                    return // Skip duplicate content
                }
                lastSignature = sig
            }

            // Notify listener
            try {
                listener.onClipboardChange(content)
            } catch (e: Throwable) {
                // Listener exceptions must not break the monitor.
                errorHandler?.invoke(e)
            }
        } catch (e: Throwable) {
            // Swallow all to keep the loop resilient.
            errorHandler?.invoke(e)
        }
    }

    private fun readClipboard(): ClipboardContent {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents: Transferable = clipboard.getContents(null)
            ?: return ClipboardContent(timestamp = System.currentTimeMillis())

        var text: String? = null
        var html: String? = null
        var rtf: String? = null
        var files: List<String>? = null
        val imageAvailable: Boolean = contents.isDataFlavorSupported(DataFlavor.imageFlavor)

        // Plain text
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            runCatching {
                text = contents.getTransferData(DataFlavor.stringFlavor) as? String
            }
        }

        // HTML (as String)
        runCatching {
            val htmlFlavor = DataFlavor("text/html;class=java.lang.String")
            if (contents.isDataFlavorSupported(htmlFlavor)) {
                html = contents.getTransferData(htmlFlavor) as? String
            }
        }

        // RTF (as String) – many apps provide RTF in this flavor on macOS
        runCatching {
            val rtfFlavor = DataFlavor("text/rtf;class=java.lang.String")
            if (contents.isDataFlavorSupported(rtfFlavor)) {
                rtf = contents.getTransferData(rtfFlavor) as? String
            }
        }

        // File list
        if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            runCatching {
                @Suppress("UNCHECKED_CAST")
                val list = contents.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                files = list.map { it.absolutePath }
            }
        }

        return ClipboardContent(
            text = text,
            html = html,
            rtf = rtf,
            files = files,
            imageAvailable = imageAvailable,
            timestamp = System.currentTimeMillis(),
        )
    }

    /**
     * Build a robust signature to avoid emitting duplicate consecutive events.
     * We hash the shapes/lengths instead of full payloads to stay cheap.
     */
    private fun signatureOf(c: ClipboardContent): String {
        val sb = StringBuilder(128)
        fun add(name: String, v: String?) {
            if (v != null) {
                sb.append(name).append('#').append(v.length).append(';')
                // Include a small prefix to disambiguate same-length strings
                sb.append(v.take(64)).append('|')
            } else {
                sb.append(name).append('#').append("0;|")
            }
        }
        add("t", c.text)
        add("h", c.html)
        add("r", c.rtf)
        sb.append("i#").append(if (c.imageAvailable) 1 else 0).append('|')
        if (c.files != null) {
            sb.append("f#").append(c.files.size).append('|')
            c.files.take(8).forEach { sb.append(it).append('|') }
        } else {
            sb.append("f#0|")
        }

        // Cheap stable digest
        return sha1(sb.toString())
    }
}

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

import kotlinx.datetime.Clock

/**
 * Stub implementation of [ClipboardMonitor] for unsupported platforms (WasmJS).
 *
 * This implementation does nothing and always returns empty clipboard content.
 * It's used as a placeholder for platforms where clipboard monitoring is not
 * yet supported or not feasible.
 *
 * **Behavior:**
 * - [start] and [stop] do nothing
 * - [isRunning] always returns false
 * - [getCurrentContent] always returns empty content with current timestamp
 *
 * **Note:** For production use on web platforms, prefer the standard JS target
 * which has full clipboard monitoring support via the browser's Clipboard API.
 *
 * @see ClipboardMonitor
 * @since 1.0.0
 */
internal class StubClipboardMonitor : ClipboardMonitor {
    /**
     * Does nothing. Clipboard monitoring is not supported.
     */
    override fun start() {
        // No-op: Stub implementation
    }

    /**
     * Does nothing. Clipboard monitoring is not supported.
     */
    override fun stop() {
        // No-op: Stub implementation
    }

    /**
     * Always returns false as monitoring is not supported.
     *
     * @return false
     */
    override fun isRunning(): Boolean = false

    /**
     * Returns empty clipboard content with current timestamp.
     *
     * @return Empty [ClipboardContent] with all fields null except timestamp
     */
    override fun getCurrentContent(): ClipboardContent {
        return ClipboardContent(timestamp = Clock.System.now().toEpochMilliseconds())
    }
}

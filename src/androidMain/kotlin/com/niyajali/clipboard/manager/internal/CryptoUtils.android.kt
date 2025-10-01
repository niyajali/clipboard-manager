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
package com.niyajali.clipboard.manager.internal

import java.security.MessageDigest

/**
 * Android implementation of SHA-1 hashing using Java's MessageDigest.
 */
internal actual fun sha1(input: String): String {
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

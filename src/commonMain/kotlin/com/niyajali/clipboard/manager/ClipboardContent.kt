package com.niyajali.clipboard.manager

/**
 * Represents the content currently stored in the system clipboard.
 *
 * This data class encapsulates various formats of clipboard data that may be present
 * simultaneously. Not all platforms support all formats.
 *
 * @property text Plain text content from the clipboard. Supported on all platforms.
 * @property html HTML formatted content from the clipboard. May be null if no HTML content is present.
 *                Supported on: Android, JVM (Windows, macOS, Linux), JS/WasmJS.
 * @property rtf Rich Text Format content. May be null if no RTF content is present.
 *               Supported on: JVM (Windows, macOS). Not available on Android.
 * @property files List of file paths or URIs copied to the clipboard.
 *                 On Android: Content URIs (content://...).
 *                 On JVM: Absolute file paths.
 *                 On JS/WasmJS: File objects (implementation-specific).
 *                 May be null if no files are present.
 * @property imageAvailable Indicates whether an image is available in the clipboard.
 *                          This is a boolean flag; actual image data must be retrieved
 *                          through platform-specific APIs.
 * @property timestamp Unix timestamp (milliseconds) when this content was captured.
 *
 * @see ClipboardMonitor.getCurrentContent
 * @since 1.0.0
 */
public data class ClipboardContent(
    val text: String? = null,
    val html: String? = null,
    val rtf: String? = null,
    val files: List<String>? = null,
    val imageAvailable: Boolean = false,
    val timestamp: Long
) {
    /**
     * Checks if the clipboard contains any content.
     *
     * @return `true` if at least one content field is non-null or non-empty, `false` otherwise.
     */
    public fun isEmpty(): Boolean =
        text.isNullOrEmpty() && html.isNullOrEmpty() && rtf.isNullOrEmpty() &&
                files.isNullOrEmpty() && !imageAvailable

    /**
     * Checks if the clipboard contains content.
     *
     * @return `true` if at least one content field has data, `false` otherwise.
     */
    public fun isNotEmpty(): Boolean = !isEmpty()

    /**
     * Returns a concise string representation of the content types present.
     *
     * Example: "text(42 chars), html, 2 file(s), image"
     */
    override fun toString(): String = buildString {
        val parts = mutableListOf<String>()
        if (!text.isNullOrEmpty()) parts.add("text(${text.length} chars)")
        if (!html.isNullOrEmpty()) parts.add("html")
        if (!rtf.isNullOrEmpty()) parts.add("rtf")
        if (!files.isNullOrEmpty()) parts.add("${files.size} file(s)")
        if (imageAvailable) parts.add("image")
        if (parts.isEmpty()) parts.add("empty")
        append(parts.joinToString(", "))
    }
}

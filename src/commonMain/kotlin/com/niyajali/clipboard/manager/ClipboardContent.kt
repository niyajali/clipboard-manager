package com.niyajali.clipboard.manager

public data class ClipboardContent(
    val text: String? = null,
    val html: String? = null,
    val rtf: String? = null,
    val files: List<String>? = null,
    val imageAvailable: Boolean = false,
    val timestamp: Long
)
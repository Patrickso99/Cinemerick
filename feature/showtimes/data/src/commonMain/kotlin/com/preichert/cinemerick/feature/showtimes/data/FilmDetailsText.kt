package com.preichert.cinemerick.feature.showtimes.data

private val PARAGRAPH_END = Regex("</p\\s*>|<br\\s*/?>", RegexOption.IGNORE_CASE)
private val TAG = Regex("<[^>]*>")
private val SPACES = Regex("[ \\t\\u00A0]+")

/** Plain text of an HTML fragment, one paragraph per line; null when nothing readable is left. */
internal fun String?.htmlToPlainText(): String? =
    this?.replace(PARAGRAPH_END, "\n")
        ?.replace(TAG, "")
        ?.unescapeHtml()
        ?.lines()
        ?.map { it.replace(SPACES, " ").trim() }
        ?.filter { it.isNotEmpty() }
        ?.joinToString("\n")
        ?.ifEmpty { null }

internal fun String?.orNullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

internal fun List<String>.joinedOrNull(separator: String = ", "): String? =
    map { it.trim() }.filter { it.isNotEmpty() }.joinToString(separator).ifEmpty { null }

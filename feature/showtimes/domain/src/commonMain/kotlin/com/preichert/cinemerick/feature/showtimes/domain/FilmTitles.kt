package com.preichert.cinemerick.feature.showtimes.domain

private val whitespace = Regex("\\s+")

fun cleanTitle(raw: String): String {
    val title = raw.trim().replace(whitespace, " ")
    val isAllCaps = title.any { it.isLetter() } && title == title.uppercase()
    return if (isAllCaps) title.toTitleCase() else title
}

// Two cinemas name the same film differently ("Naza" / "Naza C.A.", "X - Sottotitolo"): compare on this key.
fun filmKey(title: String): String =
    title.lowercase()
        .replace(":", "")
        .replace(" c.a.", "")
        .split(" - ")
        .first()
        .trim()

private fun String.toTitleCase(): String {
    val result = StringBuilder(length)
    var previousIsLetter = false
    for (char in lowercase()) {
        result.append(if (!previousIsLetter && char.isLetter()) char.uppercaseChar() else char)
        previousIsLetter = char.isLetter()
    }
    return result.toString()
}

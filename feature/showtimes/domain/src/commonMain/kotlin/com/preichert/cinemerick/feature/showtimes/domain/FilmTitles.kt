package com.preichert.cinemerick.feature.showtimes.domain

private val whitespace = Regex("\\s+")
private val specialChars = Regex("[^a-z0-9 ]")
private val languageSuffixPattern = Regex(""" - (Lingua originale|Original Language|VO|Dubbed|20 Anni|Extended|Director'?s).*$""", RegexOption.IGNORE_CASE)
private val diacriticsMap = mapOf(
    'à' to 'a', 'á' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a', 'å' to 'a',
    'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ë' to 'e',
    'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ï' to 'i',
    'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o',
    'ù' to 'u', 'ú' to 'u', 'û' to 'u', 'ü' to 'u',
    'ñ' to 'n', 'ç' to 'c',
    'À' to 'A', 'Á' to 'A', 'Â' to 'A', 'Ã' to 'A', 'Ä' to 'A', 'Å' to 'A',
    'È' to 'E', 'É' to 'E', 'Ê' to 'E', 'Ë' to 'E',
    'Ì' to 'I', 'Í' to 'I', 'Î' to 'I', 'Ï' to 'I',
    'Ò' to 'O', 'Ó' to 'O', 'Ô' to 'O', 'Õ' to 'O', 'Ö' to 'O',
    'Ù' to 'U', 'Ú' to 'U', 'Û' to 'U', 'Ü' to 'U',
    'Ñ' to 'N', 'Ç' to 'C'
)

fun cleanTitle(raw: String): String {
    val title = raw.trim().replace(whitespace, " ")
    val isAllCaps = title.any { it.isLetter() } && title == title.uppercase()
    return if (isAllCaps) title.toTitleCase() else title
}

// Two cinemas name the same film differently ("Naza" / "Naza C.A.", "X - Sottotitolo"): compare on this key.
// Also handles diacritics: "La città dei vivi" == "La citta dei vivi" == "La Citta' Dei Vivi"
// Removes all special characters to handle punctuation variants: "Coyote Vs Acme" == "Coyote Vs. Acme"
// Normalizes language/format suffixes: "Linkin Park: Unshatter" == "LINKIN PARK UNSHATTER - Lingua originale"
fun filmKey(title: String): String {
    val normalized = title.map { diacriticsMap[it] ?: it }.joinToString("")
    val lowercased = normalized.lowercase().replace(" c.a.", "")
    val hasColonAndDash = lowercased.contains(":") && lowercased.contains(" - ")

    return if (hasColonAndDash) {
        // When colon and dash both exist, the dash is likely a language suffix
        // Remove language suffix and split on colon-to-dash conversion, keeping only main title
        lowercased.replace(languageSuffixPattern, "")
            .replace(": ", " - ")
            .split(" - ")
            .first()
    } else if (lowercased.contains(":")) {
        // Colon without language suffix: keep colon content by converting to space
        lowercased.replace(languageSuffixPattern, "")
            .replace(": ", " ")
    } else {
        // No colon: use original split logic for dash-separated subtitles
        lowercased.replace(languageSuffixPattern, "")
            .replace(": ", " - ")
            .split(" - ")
            .first()
    }
    .replace(specialChars, "")
    .replace(whitespace, " ")
    .trim()
}

private fun String.toTitleCase(): String {
    val result = StringBuilder(length)
    var previousIsLetter = false
    for (char in lowercase()) {
        result.append(if (!previousIsLetter && char.isLetter()) char.uppercaseChar() else char)
        previousIsLetter = char.isLetter()
    }
    return result.toString()
}

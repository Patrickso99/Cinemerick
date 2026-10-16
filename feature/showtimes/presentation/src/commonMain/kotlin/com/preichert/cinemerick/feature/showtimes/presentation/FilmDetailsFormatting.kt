package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** "2 h 05 min", or "45 min" under an hour. */
internal fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return if (hours == 0) "$minutes min" else "$hours h ${rest.toString().padStart(2, '0')} min"
}

/** The film's year and runtime on one line ("1977 · 2 h 01 min"), or null when it has neither. */
internal fun FilmDetails.metaLine(): String? =
    listOfNotNull(year?.toString(), runtimeMinutes?.let(::formatRuntime)).takeIf { it.isNotEmpty() }?.joinToString(" · ")

/** One decimal, with a dot: 8.207 gives "8.2". */
internal fun formatScore(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

/** The original title, unless it only repeats the title the sheet already shows. */
internal fun FilmDetails.originalTitleFor(shownTitle: String): String? =
    originalTitle?.takeUnless { it.trim().equals(shownTitle.trim(), ignoreCase = true) }

/**
 * The name of an original language (TMDB gives ISO 639-1 codes) in the language of the app, from the platform's own
 * locale data, or null for a code the platform cannot name.
 */
internal expect fun languageDisplayName(code: String): String?

/** The language code the sheet falls back to when the app has no name for the language. */
internal fun languageCodeFallback(code: String): String = code.trim().uppercase()

/** US dollars in a short form that reads the same in both languages: "$850K", "$5.5M", "$190M", "$1.2B". */
internal fun formatMoney(amount: Long): String = when {
    amount >= 995_000_000L -> "$${oneDecimal(amount / 1e9)}B"
    amount >= 9_950_000L -> "$${(amount / 1e6).roundToLong()}M"
    amount >= 995_000L -> "$${oneDecimal(amount / 1e6)}M"
    amount >= 1_000L -> "$${(amount / 1e3).roundToLong()}K"
    else -> "$$amount"
}

// One decimal, left out when it is zero: 1.0 gives "1", 1.24 gives "1.2".
private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToLong()
    return if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
}

/** "28/02/2024": the numeric format the rest of the app uses for dates. */
internal fun formatReleaseDate(date: LocalDate): String =
    "${date.day.toString().padStart(2, '0')}/${date.month.number.toString().padStart(2, '0')}/${date.year}"

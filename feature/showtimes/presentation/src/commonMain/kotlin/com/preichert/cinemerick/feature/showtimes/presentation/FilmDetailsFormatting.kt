package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlin.math.roundToInt

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

package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalTime

sealed interface ParsedTime {
    data class Valid(val value: LocalTime?) : ParsedTime
    data object Invalid : ParsedTime
}

private val END_OF_DAY = LocalTime(23, 59)

/**
 * Parses user time input: blank means "no bound", `H`/`HH` means `HH:00`, `H:MM`/`HH:MM` is
 * taken as is, and `24` or `24:00` means the end of the day (23:59).
 */
fun parseTimeInput(text: String): ParsedTime {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return ParsedTime.Valid(null)

    val parts = trimmed.split(":")
    if (parts.size > 2) return ParsedTime.Invalid
    val hour = parts[0].toIntOrNull()?.takeIf { parts[0].length in 1..2 && parts[0].all(Char::isDigit) }
        ?: return ParsedTime.Invalid
    val minute = if (parts.size == 1) {
        0
    } else {
        parts[1].toIntOrNull()?.takeIf { parts[1].length == 2 && parts[1].all(Char::isDigit) }
            ?: return ParsedTime.Invalid
    }

    return when {
        hour == 24 && minute == 0 -> ParsedTime.Valid(END_OF_DAY)
        hour in 0..23 && minute in 0..59 -> ParsedTime.Valid(LocalTime(hour, minute))
        else -> ParsedTime.Invalid
    }
}

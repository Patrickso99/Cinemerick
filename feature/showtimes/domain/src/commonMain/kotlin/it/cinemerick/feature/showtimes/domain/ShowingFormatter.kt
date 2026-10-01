package it.cinemerick.feature.showtimes.domain

import kotlinx.datetime.DayOfWeek

fun DayOfWeek.italianName(): String = when (this) {
    DayOfWeek.MONDAY -> "Lunedì"
    DayOfWeek.TUESDAY -> "Martedì"
    DayOfWeek.WEDNESDAY -> "Mercoledì"
    DayOfWeek.THURSDAY -> "Giovedì"
    DayOfWeek.FRIDAY -> "Venerdì"
    DayOfWeek.SATURDAY -> "Sabato"
    DayOfWeek.SUNDAY -> "Domenica"
}

fun Showing.toPollLine(): String =
    "$title (${day.dayOfWeek.italianName()} - $time - ${cinema.displayName})"

fun List<FilmGroup>.toPollText(): String =
    flatMap { group -> group.showings.map { it.toPollLine() } }.joinToString("\n")

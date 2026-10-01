package com.preichert.cinemerick.feature.showtimes.domain

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

fun Showing.toPollLine(title: String = this.title): String =
    "$title (${day.dayOfWeek.italianName()} - $time - ${cinema.displayName}${format?.let { " - $it" }.orEmpty()})"

fun List<FilmGroup>.toPollText(): String =
    flatMap { group -> group.showings.map { it.toPollLine(group.title) } }.joinToString("\n")

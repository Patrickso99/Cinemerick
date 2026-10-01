package it.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class DayRange(
    val date: LocalDate,
    val min: LocalTime? = null,
    val max: LocalTime? = null
)

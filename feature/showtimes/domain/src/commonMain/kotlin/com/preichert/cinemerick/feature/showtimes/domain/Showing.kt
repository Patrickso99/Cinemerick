package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Showing(
    val title: String,
    val day: LocalDate,
    val time: LocalTime,
    val venue: Venue,
    val format: String? = null,
    val posterUrl: String? = null
)

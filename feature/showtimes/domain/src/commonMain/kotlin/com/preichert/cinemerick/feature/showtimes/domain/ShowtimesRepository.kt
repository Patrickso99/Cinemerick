package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate

interface ShowtimesRepository {
    suspend fun getShowings(days: List<LocalDate>): List<CinemaShowings>
}

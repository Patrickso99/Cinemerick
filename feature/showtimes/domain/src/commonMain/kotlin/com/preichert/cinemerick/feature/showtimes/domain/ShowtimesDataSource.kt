package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlinx.datetime.LocalDate

interface ShowtimesDataSource {
    val cinema: Cinema
    suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network>
}

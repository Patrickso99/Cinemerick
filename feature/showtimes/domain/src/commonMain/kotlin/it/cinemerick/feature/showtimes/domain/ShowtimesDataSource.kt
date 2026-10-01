package it.cinemerick.feature.showtimes.domain

import it.cinemerick.core.domain.DataError
import it.cinemerick.core.domain.Result
import kotlinx.datetime.LocalDate

interface ShowtimesDataSource {
    val cinema: Cinema
    suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network>
}

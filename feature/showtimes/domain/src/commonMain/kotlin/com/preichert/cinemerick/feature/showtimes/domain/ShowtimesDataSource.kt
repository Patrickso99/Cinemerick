package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlinx.datetime.LocalDate

interface ShowtimesDataSource {
    val chain: Chain
    suspend fun getVenues(): Result<List<Venue>, DataError.Network>
    suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network>
}

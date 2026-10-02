package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlinx.datetime.LocalDate

interface ShowtimesRepository {
    suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>>
    suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings>
}

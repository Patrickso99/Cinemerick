package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlinx.datetime.LocalDate

interface ShowtimesDataSource {
    val chain: Chain
    suspend fun getVenues(): Result<List<Venue>, DataError.Network>
    suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network>

    /** Details from a film page of this chain, for chains whose showtimes do not carry them. */
    suspend fun getFilmDetails(detailsUrl: String): Result<FilmDetails, DataError.Network> = Result.Success(FilmDetails())
}

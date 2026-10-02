package com.preichert.cinemerick.feature.showtimes.data

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import com.preichert.cinemerick.core.data.get
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.map
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

class KtorTheSpaceShowtimesDataSource(
    private val httpClient: HttpClient,
    private val tokenProvider: TheSpaceTokenProvider
) : ShowtimesDataSource {

    override val chain = Chain.THE_SPACE

    override suspend fun getVenues(): Result<List<Venue>, DataError.Network> {
        val token = when (val result = tokenProvider.getToken()) {
            is Result.Failure -> return result
            is Result.Success -> result.data
        }
        return httpClient.get<TheSpaceCinemasDto>(
            url = CINEMAS_URL,
            headers = mapOf(HttpHeaders.Authorization to "Bearer $token")
        ).map { dto ->
            dto.result.flatMap { group ->
                group.cinemas.map { cinema ->
                    Venue(
                        chain = Chain.THE_SPACE,
                        id = cinema.cinemaId,
                        name = cinema.cinemaName,
                        region = null,
                        webUrl = cinema.whatsOnUrl
                    )
                }
            }
        }
    }

    override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> {
        val token = when (val result = tokenProvider.getToken()) {
            is Result.Failure -> return result
            is Result.Success -> result.data
        }
        return coroutineScope {
            days.map { day -> async { fetchDay(venue, day, token) } }.awaitAll().mergeResults()
        }
    }

    private suspend fun fetchDay(venue: Venue, day: LocalDate, token: String): Result<List<Showing>, DataError.Network> {
        val first = requestDay(venue, day, token)
        if (first !is Result.Failure || first.error != DataError.Network.UNAUTHORIZED) return first

        // Token expired: fetch a fresh one and retry once.
        val freshToken = when (val result = tokenProvider.getToken(forceRefresh = true)) {
            is Result.Failure -> return result
            is Result.Success -> result.data
        }
        return requestDay(venue, day, freshToken)
    }

    private suspend fun requestDay(venue: Venue, day: LocalDate, token: String): Result<List<Showing>, DataError.Network> =
        httpClient.get<TheSpaceFilmsDto>(
            url = "https://www.thespacecinema.it/api/microservice/showings/cinemas/${venue.id}/films" +
                "?showingDate=${day}T00:00:00&minEmbargoLevel=3" +
                "&includesSession=true&includeSessionAttributes=true",
            headers = mapOf(HttpHeaders.Authorization to "Bearer $token")
        ).map { it.toShowings(venue, day) }

    private companion object {
        const val CINEMAS_URL = "https://www.thespacecinema.it/api/microservice/showings/cinemas"
    }
}

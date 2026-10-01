package com.preichert.cinemerick.feature.showtimes.data

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import com.preichert.cinemerick.core.data.get
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.map
import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

class KtorSpaceShowtimesDataSource(
    private val httpClient: HttpClient,
    private val tokenProvider: SpaceTokenProvider
) : ShowtimesDataSource {

    override val cinema = Cinema.SILEA

    override suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network> {
        val token = when (val result = tokenProvider.getToken()) {
            is Result.Error -> return result
            is Result.Success -> result.data
        }
        return coroutineScope {
            days.map { day -> async { fetchDay(day, token) } }.awaitAll().mergeResults()
        }
    }

    private suspend fun fetchDay(day: LocalDate, token: String): Result<List<Showing>, DataError.Network> {
        val first = requestDay(day, token)
        if (first !is Result.Error || first.error != DataError.Network.UNAUTHORIZED) return first

        // Token expired: fetch a fresh one and retry once.
        val freshToken = when (val result = tokenProvider.getToken(forceRefresh = true)) {
            is Result.Error -> return result
            is Result.Success -> result.data
        }
        return requestDay(day, freshToken)
    }

    private suspend fun requestDay(day: LocalDate, token: String): Result<List<Showing>, DataError.Network> =
        httpClient.get<SpaceFilmsDto>(
            url = "$API_URL?showingDate=${day}T00:00:00&minEmbargoLevel=3" +
                "&includesSession=true&includeSessionAttributes=true",
            headers = mapOf(HttpHeaders.Authorization to "Bearer $token")
        ).map { it.toShowings(day) }

    private companion object {
        const val API_URL = "https://www.thespacecinema.it/api/microservice/showings/cinemas/1009/films"
    }
}

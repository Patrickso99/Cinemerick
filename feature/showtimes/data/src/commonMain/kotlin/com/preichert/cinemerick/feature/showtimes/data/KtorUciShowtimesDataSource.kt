package com.preichert.cinemerick.feature.showtimes.data

import io.ktor.client.HttpClient
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

class KtorUciShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val cinema = Cinema.UCI

    override suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        coroutineScope {
            days.map { day -> async { fetchDay(day) } }.awaitAll().mergeResults()
        }

    private suspend fun fetchDay(day: LocalDate): Result<List<Showing>, DataError.Network> =
        httpClient.get<UciProgrammingDto>("$BASE_URL/$day").map { it.toShowings(day) }

    private companion object {
        const val BASE_URL =
            "https://myuci---uci-backend-production-nfluwp7wga-oc.a.run.app" +
                "/api/theatres/uci-cinemas-venezia-marcon/programming"
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import io.ktor.client.HttpClient
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

class KtorUciShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val chain = Chain.UCI

    override suspend fun getVenues(): Result<List<Venue>, DataError.Network> =
        httpClient.get<UciTheatresDto>(THEATRES_URL).map { dto ->
            dto.data.map { theatre ->
                Venue(
                    chain = Chain.UCI,
                    id = theatre.slug,
                    name = theatre.name.removePrefix("UCI Cinemas "),
                    region = theatre.region.ifEmpty { null },
                    webUrl = "https://ucicinemas.it/cinema/${theatre.slug}"
                )
            }
        }

    override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        coroutineScope {
            days.map { day -> async { fetchDay(venue, day) } }.awaitAll().mergeResults()
        }

    private suspend fun fetchDay(venue: Venue, day: LocalDate): Result<List<Showing>, DataError.Network> =
        httpClient.get<UciProgrammingDto>("$THEATRES_URL_BASE/${venue.id}/programming/$day").map { it.toShowings(venue, day) }

    private companion object {
        const val THEATRES_URL = "https://myuci---uci-backend-production-nfluwp7wga-oc.a.run.app/api/theatres"
        const val THEATRES_URL_BASE = "https://myuci---uci-backend-production-nfluwp7wga-oc.a.run.app/api/theatres"
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import com.preichert.cinemerick.core.data.getResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.core.data.DispatcherProvider
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("ArcadiaShowtimes")

// Arcadia Cinemas: the cinemas are read from the home page's menu, and each cinema page lists its whole programme (one request, whatever the days).
class KtorArcadiaShowtimesDataSource(
    private val httpClient: HttpClient,
    private val dispatchers: DispatcherProvider
) : ShowtimesDataSource {

    override val chain = Chain.ARCADIA

    override suspend fun getVenues(): Result<List<Venue>, DataError.Network> =
        when (val response = httpClient.getResponse(ARCADIA_BASE_URL)) {
            is Result.Failure -> response
            is Result.Success -> {
                val venues = withContext(dispatchers.parsing) { parseArcadiaVenues(response.data.bodyAsText()) }
                log.d { "${venues.size} venue(s)" }
                Result.Success(venues)
            }
        }

    override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        when (val response = httpClient.getResponse("$ARCADIA_BASE_URL/${venue.id}")) {
            is Result.Failure -> response
            is Result.Success -> {
                val body = response.data.bodyAsText()
                val showings = withContext(dispatchers.parsing) { parseArcadiaShowings(body, venue, days) }
                log.d { "${venue.id}: ${body.length} chars, ${showings.size} showing(s)" }
                Result.Success(showings)
            }
        }

    override suspend fun getFilmDetails(detailsUrl: String): Result<FilmDetails, DataError.Network> =
        httpClient.fetchFilmPage(detailsUrl, dispatchers, ::parseArcadiaFilmPage)
}

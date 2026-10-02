package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import com.preichert.cinemerick.core.data.getResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("NotoriousShowtimes")

// Notorious has no central API: its pages are server-rendered HTML, one per venue listing one week, and `?week=next` the following one.
class KtorNotoriousShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val chain = Chain.NOTORIOUS

    override suspend fun getVenues(): Result<List<Venue>, DataError.Network> =
        when (val response = httpClient.getResponse(HOME_URL)) {
            is Result.Failure -> response
            is Result.Success -> {
                val html = response.data.bodyAsText()
                Result.Success(parseNotoriousVenues(html))
            }
        }

    override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        coroutineScope {
            val baseUrl = "https://www.notoriouscinemas.it/${venue.id}/index.php"
            listOf(baseUrl, "$baseUrl?week=next")
                .map { url -> async { fetchWeek(url, venue, days) } }
                .awaitAll()
                .mergeResults()
        }

    private suspend fun fetchWeek(url: String, venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        when (val response = httpClient.getResponse(url)) {
            is Result.Failure -> response
            is Result.Success -> {
                val html = response.data.bodyAsText()
                val showings = parseNotoriousShowings(html, venue, days)
                log.d { "$url: ${html.length} chars, ${showings.size} showing(s) for ${days.size} day(s)" }
                Result.Success(showings)
            }
        }

    private companion object {
        const val HOME_URL = "https://www.notoriouscinemas.it"
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import com.preichert.cinemerick.core.data.getResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("CinergiaShowtimes")

// Cinergia Conegliano runs on 18tickets: no JSON API, a day is a script response carrying server-rendered HTML.
class KtorCinergiaShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val cinema = Cinema.CINERGIA

    // The site answers 429 to bursts of requests, so only a couple of days are fetched at a time.
    private val permits = Semaphore(MAX_CONCURRENT_REQUESTS)

    override suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        coroutineScope {
            days.map { day -> async { permits.withPermit { fetchDay(day) } } }.awaitAll().mergeResults()
        }

    private suspend fun fetchDay(day: LocalDate): Result<List<Showing>, DataError.Network> =
        when (val response = httpClient.getResponse("$BASE_URL/film/fetch_films.js?date=$day&cinema=&district=&technology=&month=", AJAX_HEADERS)) {
            is Result.Error -> response
            is Result.Success -> {
                val html = response.data.bodyAsText()
                val showings = parseCinergiaShowings(html, day)
                log.d { "$day: ${html.length} chars, ${showings.size} showing(s)" }
                Result.Success(showings)
            }
        }

    private companion object {
        const val BASE_URL = "https://coneglianocinergia.18tickets.it"
        const val MAX_CONCURRENT_REQUESTS = 2

        // The `.js` suffix picks the format: an Accept header would be sent twice (the client adds its own JSON one) and make the server fail with 500.
        // The call is the site's own AJAX one, so it is marked as such.
        val AJAX_HEADERS = mapOf("X-Requested-With" to "XMLHttpRequest")
    }
}

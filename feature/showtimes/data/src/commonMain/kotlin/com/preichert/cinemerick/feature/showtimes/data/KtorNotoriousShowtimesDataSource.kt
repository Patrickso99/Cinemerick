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
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("NotoriousShowtimes")

// Notorious Ferrara has no API: its page is server-rendered HTML listing one week, and `?week=next` the following one.
class KtorNotoriousShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val cinema = Cinema.NOTORIOUS

    override suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        coroutineScope {
            listOf(PROGRAMMING_URL, "$PROGRAMMING_URL?week=next")
                .map { url -> async { fetchWeek(url, days) } }
                .awaitAll()
                .mergeResults()
        }

    private suspend fun fetchWeek(url: String, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        when (val response = httpClient.getResponse(url)) {
            is Result.Error -> response
            is Result.Success -> {
                val html = response.data.bodyAsText()
                val showings = parseNotoriousShowings(html, days)
                log.d { "$url: ${html.length} chars, ${showings.size} showing(s) for ${days.size} day(s)" }
                Result.Success(showings)
            }
        }

    private companion object {
        const val PROGRAMMING_URL = "https://www.notoriouscinemas.it/ferrara/index.php"
    }
}

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
import kotlinx.datetime.number

private val log = Logger.withTag("CristalloShowtimes")

// Cinema Cristallo Oderzo is a WordPress site: no JSON API, but the page's own day-switch AJAX call also answers a GET.
class KtorCristalloShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val cinema = Cinema.CRISTALLO

    // One request per day: only a couple at a time, to go easy on a small WordPress site.
    private val permits = Semaphore(MAX_CONCURRENT_REQUESTS)

    override suspend fun getShowings(days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        coroutineScope {
            days.map { day -> async { permits.withPermit { fetchDay(day) } } }.awaitAll().mergeResults()
        }

    private suspend fun fetchDay(day: LocalDate): Result<List<Showing>, DataError.Network> =
        when (val response = httpClient.getResponse("$AJAX_URL?action=$ACTION&param=$PARAM&date=${day.siteFormat()}&option=$OPTION")) {
            is Result.Error -> response
            is Result.Success -> {
                val body = response.data.bodyAsText()
                val showings = parseCristalloShowings(body, day)
                log.d { "$day: ${body.length} chars, ${showings.size} showing(s)" }
                Result.Success(showings)
            }
        }

    // The site wants "05-10-2026".
    private fun LocalDate.siteFormat() = "${day.toString().padStart(2, '0')}-${month.number.toString().padStart(2, '0')}-$year"

    private companion object {
        const val AJAX_URL = "https://www.cinemacristallo.com/wp-admin/admin-ajax.php"
        const val ACTION = "amy_movie_ajax_shortcode_showtime_layout_3"
        const val MAX_CONCURRENT_REQUESTS = 3

        // The theme's own settings, as the page embeds them (base64 JSON): all movies, newest first / image size and visible fields.
        const val PARAM = "eyJvcmRlcmJ5IjoiZGF0ZSIsIm9yZGVyIjoiREVTQyIsInBvc3RzX3Blcl9wYWdlIjoiLTEiLCJwYWdlZCI6MCwibW92aWVfdHlwZSI6ImFsbCIsImN1c3RvbV9maWVsZHMiOltdfQ=="
        const val OPTION = "eyJpbWFnZV9zaXplIjp7IndpZHRoIjoiMjgwIiwiaGVpZ2h0IjoiNDAwIiwiaXNfY3JvcCI6IjEifSwiZ2VuZXJhbF9maWVsZHNfdG9vbHRpcCI6WyJ0aXRsZSIsImNvbnRlbnQiLCJ0cmFsaWVyIiwiZGV0YWlsIiwicmF0ZSIsIm1wYWEiLCJpbWRiIiwiZHVyYXRpb24iXSwibGlzdF9maWVsZHNfdmlzaWJsZSI6Im1vdmllX3JlbGVhc2UsbW92aWVfaW1kYixtb3ZpZV9sYW5ndWFnZSxtb3ZpZV9nZW5yZSxtb3ZpZV9hY3Rvcixtb3ZpZV9kaXJlY3Rvcixtb3ZpZV9jaW5lbWEiLCJzdGFydF9kYXRlIjoiMTBcLzAxXC8yNiJ9"
    }
}

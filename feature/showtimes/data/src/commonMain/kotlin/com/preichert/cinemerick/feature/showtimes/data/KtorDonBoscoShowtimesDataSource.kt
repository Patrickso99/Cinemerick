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
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("DonBoscoShowtimes")

// Cinema Don Bosco (San Donà di Piave) lists its whole programme on the home page: one request, whatever the days.
class KtorDonBoscoShowtimesDataSource(
    private val httpClient: HttpClient
) : ShowtimesDataSource {

    override val chain = Chain.DON_BOSCO

    override suspend fun getVenues(): Result<List<Venue>, DataError.Network> =
        Result.Success(listOf(
            Venue(Chain.DON_BOSCO, "san-dona-di-piave", "San Donà di Piave", "Veneto", HOME_URL)
        ))

    override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
        when (val response = httpClient.getResponse(HOME_URL)) {
            is Result.Failure -> response
            is Result.Success -> {
                val body = response.data.bodyAsText()
                val showings = parseDonBoscoShowings(body, venue, days)
                log.d { "${body.length} chars, ${showings.size} showing(s)" }
                Result.Success(showings)
            }
        }

    private companion object {
        const val HOME_URL = "https://cinema.donboscosandona.it/"
    }
}

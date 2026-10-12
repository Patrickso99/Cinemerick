package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UciShowingMappersTest {

    private val day = LocalDate(2026, 10, 12)
    private val venue = Venue(Chain.UCI, "uci-cinemas-marcon", "Marcon")

    private fun performance(time: String, cartLink: String?) =
        UciPerformanceDto(day = "2026-10-12", actualStartAt = time, cartLink = cartLink)

    private fun programming(vararg performances: UciPerformanceDto) = UciProgrammingDto(
        data = listOf(
            UciMovieDto(
                title = "Mammina",
                screens = listOf(mapOf("2D" to listOf(UciVariantDto(performances = performances.toList()))))
            )
        )
    )

    @Test
    fun eachPerformanceKeepsItsOwnPurchaseUrl() {
        val showings = programming(
            performance("19:30", "/movies/mammina/acquista/5125/9491/404491"),
            performance("21:45", "/movies/mammina/acquista/5125/9491/404492")
        ).toShowings(venue, day)

        assertEquals(
            listOf(
                "https://ucicinemas.it/movies/mammina/acquista/5125/9491/404491",
                "https://ucicinemas.it/movies/mammina/acquista/5125/9491/404492"
            ),
            showings.map { it.ticketUrl }
        )
    }

    @Test
    fun missingOrBlankCartLinkGivesNoPurchaseUrl() {
        val showings = programming(performance("19:30", null), performance("21:45", "  ")).toShowings(venue, day)

        assertEquals(2, showings.size)
        assertNull(showings[0].ticketUrl)
        assertNull(showings[1].ticketUrl)
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun performancesAreNeverSoldOut() {
        val showings = programming(performance("19:30", "/a"), performance("21:45", null)).toShowings(venue, day)

        assertFalse(showings.any { it.soldOut })
    }

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    // Condensed from the programming of uci-cinemas-arezzo.
    private val realPayload = """
        {"data":[{"id":7340,"title":"Mammina","slug":"mammina","poster":"https://img/mammina.jpg",
          "trailer_link":null,
          "description":"<p>Un viaggio terrificante nel mondo di Bela &amp; sua madre.</p><p></p><p>Si ricorda che il film inizia subito.</p>",
          "rating":{"image":"https://img/14.png","name":"14+","description":null},
          "genres":[{"name":"Horror","slug":"HORROR","image":""},{"name":"Thriller","slug":"THRILLER","image":""}],
          "screens":[{"2D":[{"id":4062,"language":{"name":"ITA"},"subtitles":null,
            "performances":[{"id":1,"day":"2026-10-12","actual_start_at":"19:50","cart_link":"/movies/mammina/acquista/1/2/3","price_starting_from":"6.9"}]}]}]}]}
    """.trimIndent()

    @Test
    fun movieDetailsAreReadFromTheProgramming() {
        val showing = json.decodeFromString<UciProgrammingDto>(realPayload).toShowings(venue, day).single()

        assertEquals(
            FilmDetails(
                synopsis = "Un viaggio terrificante nel mondo di Bela & sua madre.\nSi ricorda che il film inizia subito.",
                genre = "Horror, Thriller",
                rating = "14+"
            ),
            showing.details
        )
    }

    @Test
    fun trailerLinkIsKeptWhenPublished() {
        val payload = realPayload.replace("\"trailer_link\":null", "\"trailer_link\":\"https://youtu.be/abc\"")

        assertEquals("https://youtu.be/abc", json.decodeFromString<UciProgrammingDto>(payload).toShowings(venue, day).single().details?.trailerUrl)
    }

    @Test
    fun movieWithoutAnyDetailGivesNoDetails() {
        assertNull(programming(performance("19:30", null)).toShowings(venue, day).single().details)
    }

    @Test
    fun missingOrNullDetailFieldsDoNotBreakParsing() {
        val payload = """{"data":[{"title":"Mammina","description":null,"rating":null,"genres":[],"screens":[{"2D":[{"performances":[{"day":"2026-10-12","actual_start_at":"19:50"}]}]}]}]}"""

        assertNull(json.decodeFromString<UciProgrammingDto>(payload).toShowings(venue, day).single().details)
    }
}

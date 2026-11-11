package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue

private val lenientJson = Json { ignoreUnknownKeys = true }

class TheSpaceShowingMappersTest {

    private val day = LocalDate(2026, 10, 12)
    private val venue = Venue(Chain.THE_SPACE, "1028", "Silea")

    private fun films(vararg sessions: TheSpaceSessionDto) = TheSpaceFilmsDto(
        result = listOf(
            TheSpaceFilmDto(
                filmTitle = "Mammina",
                showingGroups = listOf(TheSpaceShowingGroupDto(sessions = sessions.toList()))
            )
        )
    )

    @Test
    fun eachSessionKeepsItsOwnPurchaseUrl() {
        val showings = films(
            TheSpaceSessionDto(startTime = "2026-10-12T16:20:00", bookingUrl = "/prenotare-il-biglietto/summary/1028/HO00003608/58354"),
            TheSpaceSessionDto(startTime = "2026-10-12T19:00:00", bookingUrl = "/prenotare-il-biglietto/summary/1028/HO00003608/58355")
        ).toShowings(venue, day)

        assertThat(showings.map { it.ticketUrl }).isEqualTo(listOf(
                "https://www.thespacecinema.it/prenotare-il-biglietto/summary/1028/HO00003608/58354",
                "https://www.thespacecinema.it/prenotare-il-biglietto/summary/1028/HO00003608/58355"
            ))
    }

    @Test
    fun missingOrBlankBookingUrlGivesNoPurchaseUrl() {
        val showings = films(
            TheSpaceSessionDto(startTime = "2026-10-12T16:20:00", bookingUrl = null),
            TheSpaceSessionDto(startTime = "2026-10-12T19:00:00", bookingUrl = "")
        ).toShowings(venue, day)

        assertThat(showings.size).isEqualTo(2)
        assertThat(showings[0].ticketUrl).isNull()
        assertThat(showings[1].ticketUrl).isNull()
    }
    @Test
    fun sessionReportedSoldOutIsSoldOut() {
        val showings = films(TheSpaceSessionDto(startTime = "2026-10-12T16:20:00", isSoldOut = true)).toShowings(venue, day)

        assertThat(showings.single().soldOut).isTrue()
    }

    @Test
    fun sessionWithBookingUnavailableIsSoldOut() {
        val showings = films(
            TheSpaceSessionDto(startTime = "2026-10-12T16:20:00", isSoldOut = false, isBookingAvailable = false)
        ).toShowings(venue, day)

        assertThat(showings.single().soldOut).isTrue()
    }

    @Test
    fun bookableSessionIsNotSoldOut() {
        val showings = films(
            TheSpaceSessionDto(startTime = "2026-10-12T16:20:00", isSoldOut = false, isBookingAvailable = true)
        ).toShowings(venue, day)

        assertThat(showings.single().soldOut).isFalse()
    }

    @Test
    fun sessionWithoutAvailabilityFieldsIsNotSoldOut() {
        val dto = lenientJson.decodeFromString<TheSpaceSessionDto>("""{"startTime":"2026-10-12T16:20:00"}""")

        assertThat(films(dto).toShowings(venue, day).single().soldOut).isFalse()
    }

    // Condensed from a films response of The Space (genres and trailers come back empty).
    private val realPayload = """
        {"result":[{"filmTitle":"MAMMINA","posterImageSrc":"https://img/p.jpg","director":"Rob Savage",
          "cast":"Alejandro Nieves, Jessica Chastain, Dichen Lachman","synopsisShort":"La vincitrice del premio Oscar Jessica Chastain e' protagonista.",
          "certificate":{"name":"14","description":"","src":"https://img/14"},
          "secondaryCertificates":[{"name":"Violenza","description":""},{"name":"Armi","description":""}],
          "genres":[],"trailers":[],"hasTrailer":true,
          "showingGroups":[{"date":"2026-10-12T00:00:00","sessions":[
            {"sessionId":"58352","bookingUrl":"/prenotare/1","isSoldOut":false,"isBookingAvailable":true,"startTime":"2026-10-12T16:10:00","attributes":[]}]}]}]}
    """.trimIndent()

    @Test
    fun filmDetailsAreReadFromTheFilmsResponse() {
        val showing = lenientJson.decodeFromString<TheSpaceFilmsDto>(realPayload).toShowings(venue, day).single()

        assertThat(showing.details).isEqualTo(FilmDetails(
                synopsis = "La vincitrice del premio Oscar Jessica Chastain e' protagonista.",
                director = "Rob Savage",
                cast = "Alejandro Nieves, Jessica Chastain, Dichen Lachman",
                rating = "14+ · Violenza, Armi"
            ))
        assertThat(showing.details?.trailerUrl).isNull()
        assertThat(showing.details?.genre).isNull()
    }

    @Test
    fun suitableForEveryoneCertificateReadsPerTutti() {
        val payload = realPayload.replace("\"name\":\"14\"", "\"name\":\"t\"").replace("{\"name\":\"Violenza\",\"description\":\"\"},{\"name\":\"Armi\",\"description\":\"\"}", "")

        assertThat(lenientJson.decodeFromString<TheSpaceFilmsDto>(payload).toShowings(venue, day).single().details?.rating).isEqualTo("Per tutti")
    }

    @Test
    fun filmWithoutAnyDetailGivesNoDetails() {
        assertThat(films(TheSpaceSessionDto(startTime = "2026-10-12T16:20:00")).toShowings(venue, day).single().details).isNull()
    }
}

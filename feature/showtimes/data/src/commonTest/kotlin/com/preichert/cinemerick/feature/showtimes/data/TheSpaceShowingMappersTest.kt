package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

        assertEquals(
            listOf(
                "https://www.thespacecinema.it/prenotare-il-biglietto/summary/1028/HO00003608/58354",
                "https://www.thespacecinema.it/prenotare-il-biglietto/summary/1028/HO00003608/58355"
            ),
            showings.map { it.ticketUrl }
        )
    }

    @Test
    fun missingOrBlankBookingUrlGivesNoPurchaseUrl() {
        val showings = films(
            TheSpaceSessionDto(startTime = "2026-10-12T16:20:00", bookingUrl = null),
            TheSpaceSessionDto(startTime = "2026-10-12T19:00:00", bookingUrl = "")
        ).toShowings(venue, day)

        assertEquals(2, showings.size)
        assertNull(showings[0].ticketUrl)
        assertNull(showings[1].ticketUrl)
    }
}

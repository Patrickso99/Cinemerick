package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EighteenTicketsParserTest {

    private val day = LocalDate(2026, 10, 1)
    private val venueCinergia = Venue(Chain.CINERGIA, "conegliano", "Conegliano", null, null)

    private fun film(id: Int, title: String, date: String, vararg times: String) = """
        <a class='movie__title' href='https://x.18tickets.it/film/$id?ref_date=2026-10-01'>
        $title
        </a>
        <a class='movie__title' href='https://x.18tickets.it/film/$id?ref_date=2026-10-01'>
        <img alt='$title' class='img-fluid' src='https://cdn.18tickets.net/p/$id.jpg'>
        </a>
        <div class='schedule-section-show pb-8' id='schedule-$id'>
        <div class='time-select__place'><i class='fa fa-calendar'></i>
        Giovedì $date
        <br>
        </div>
        <ul>
        ${times.withIndex().joinToString("\n") { (index, time) -> "<a data-time='1' href='https://x/film/$id/s$index#theater-init'>\n<li class='btn'>\n<i class='fas fa-ticket-alt'></i>\n$time\n</li>\n</a>" }}
        </ul>
        </div>
    """.trimIndent()

    @Test
    fun readsTrailingOriginalLanguageMarker() {
        val html = film(1, "DIGGER - V. O.", "01/10/2026", "21:00")

        val showings = parseEighteenTicketsShowings(html, venueCinergia, day)

        assertEquals(listOf("Digger"), showings.map { it.title })
        assertEquals(listOf("VO"), showings.map { it.format })
    }

    @Test
    fun parsesFilmsTimesAndPoster() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00") + film(2, "(V.O.S.) DIGGER", "01/10/2026", "9:05")

        val showings = parseEighteenTicketsShowings(html, venueCinergia, day)

        assertEquals(listOf("Verity", "Verity", "Digger"), showings.map { it.title })
        assertEquals(listOf(LocalTime(18, 30), LocalTime(21, 0), LocalTime(9, 5)), showings.map { it.time })
        assertEquals(listOf(null, null, "VO"), showings.map { it.format })
        assertEquals("https://cdn.18tickets.net/p/1.jpg", showings.first().posterUrl)
        assertEquals(setOf(venueCinergia), showings.map { it.venue }.toSet())
        assertFalse(showings.any { it.soldOut })
    }

    @Test
    fun parsesTheEscapedScriptResponse() {
        val html = film(1, "VERITY", "01/10/2026", "18:30")
        val script = "\$(\"#movie-list\").html('" + html.replace("'", "\\'").replace("\n", "\\n").replace("</", "<\\/") + "');"

        assertEquals(listOf(LocalTime(18, 30)), parseEighteenTicketsShowings(script, venueCinergia, day).map { it.time })
    }

    @Test
    fun ignoresScheduleForAnotherDay() {
        assertEquals(emptyList(), parseEighteenTicketsShowings(film(1, "VERITY", "02/10/2026", "18:30"), venueCinergia, day))
    }

    @Test
    fun eachTimeKeepsItsOwnPurchaseUrl() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00") + film(2, "DIGGER", "01/10/2026", "9:05")

        val showings = parseEighteenTicketsShowings(html, venueCinergia, day)

        assertEquals(
            listOf(
                "https://x/film/1/s0#theater-init",
                "https://x/film/1/s1#theater-init",
                "https://x/film/2/s0#theater-init"
            ),
            showings.map { it.ticketUrl }
        )
    }

    @Test
    fun escapedScriptResponseKeepsPurchaseUrls() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00")
        val script = "\$(\"#movie-list\").html('" + html.replace("'", "\\'").replace("\n", "\\n").replace("</", "<\\/") + "');"

        assertEquals(
            listOf("https://x/film/1/s0#theater-init", "https://x/film/1/s1#theater-init"),
            parseEighteenTicketsShowings(script, venueCinergia, day).map { it.ticketUrl }
        )
    }

    @Test
    fun timeLinkWithoutUsableHrefGivesNoPurchaseUrl() {
        val withoutHref = film(1, "VERITY", "01/10/2026", "18:30").replace(" href='https://x/film/1/s0#theater-init'", "")
        val withAnchor = film(2, "DIGGER", "01/10/2026", "21:00").replace("href='https://x/film/2/s0#theater-init'", "href='#'")

        val showings = parseEighteenTicketsShowings(withoutHref + withAnchor, venueCinergia, day)

        assertEquals(listOf<String?>(null, null), showings.map { it.ticketUrl })
        assertEquals(listOf(LocalTime(18, 30), LocalTime(21, 0)), showings.map { it.time })
    }
}

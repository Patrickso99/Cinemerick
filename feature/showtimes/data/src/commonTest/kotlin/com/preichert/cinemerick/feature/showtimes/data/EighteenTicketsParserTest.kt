package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse

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

        assertThat(showings.map { it.title }).isEqualTo(listOf("Digger"))
        assertThat(showings.map { it.format }).isEqualTo(listOf("VO"))
    }

    @Test
    fun parsesFilmsTimesAndPoster() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00") + film(2, "(V.O.S.) DIGGER", "01/10/2026", "9:05")

        val showings = parseEighteenTicketsShowings(html, venueCinergia, day)

        assertThat(showings.map { it.title }).isEqualTo(listOf("Verity", "Verity", "Digger"))
        assertThat(showings.map { it.time }).isEqualTo(listOf(LocalTime(18, 30), LocalTime(21, 0), LocalTime(9, 5)))
        assertThat(showings.map { it.format }).isEqualTo(listOf(null, null, "VO"))
        assertThat(showings.first().posterUrl).isEqualTo("https://cdn.18tickets.net/p/1.jpg")
        assertThat(showings.map { it.venue }.toSet()).isEqualTo(setOf(venueCinergia))
        assertThat(showings.any { it.soldOut }).isFalse()
    }

    @Test
    fun parsesTheEscapedScriptResponse() {
        val html = film(1, "VERITY", "01/10/2026", "18:30")
        val script = "\$(\"#movie-list\").html('" + html.replace("'", "\\'").replace("\n", "\\n").replace("</", "<\\/") + "');"

        assertThat(parseEighteenTicketsShowings(script, venueCinergia, day).map { it.time }).isEqualTo(listOf(LocalTime(18, 30)))
    }

    @Test
    fun ignoresScheduleForAnotherDay() {
        assertThat(parseEighteenTicketsShowings(film(1, "VERITY", "02/10/2026", "18:30"), venueCinergia, day)).isEmpty()
    }

    @Test
    fun eachTimeKeepsItsOwnPurchaseUrl() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00") + film(2, "DIGGER", "01/10/2026", "9:05")

        val showings = parseEighteenTicketsShowings(html, venueCinergia, day)

        assertThat(showings.map { it.ticketUrl }).isEqualTo(listOf(
                "https://x/film/1/s0#theater-init",
                "https://x/film/1/s1#theater-init",
                "https://x/film/2/s0#theater-init"
            ))
    }

    @Test
    fun escapedScriptResponseKeepsPurchaseUrls() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00")
        val script = "\$(\"#movie-list\").html('" + html.replace("'", "\\'").replace("\n", "\\n").replace("</", "<\\/") + "');"

        assertThat(parseEighteenTicketsShowings(script, venueCinergia, day).map { it.ticketUrl }).isEqualTo(listOf("https://x/film/1/s0#theater-init", "https://x/film/1/s1#theater-init"))
    }

    @Test
    fun timeLinkWithoutUsableHrefGivesNoPurchaseUrl() {
        val withoutHref = film(1, "VERITY", "01/10/2026", "18:30").replace(" href='https://x/film/1/s0#theater-init'", "")
        val withAnchor = film(2, "DIGGER", "01/10/2026", "21:00").replace("href='https://x/film/2/s0#theater-init'", "href='#'")

        val showings = parseEighteenTicketsShowings(withoutHref + withAnchor, venueCinergia, day)

        assertThat(showings.map { it.ticketUrl }).isEqualTo(listOf<String?>(null, null))
        assertThat(showings.map { it.time }).isEqualTo(listOf(LocalTime(18, 30), LocalTime(21, 0)))
    }

    @Test
    fun linksEachFilmToItsFilmPageWithoutTheDateQuery() {
        val html = film(91221, "VERITY", "01/10/2026", "18:30") + film(2, "DIGGER", "01/10/2026", "21:00")

        assertThat(parseEighteenTicketsShowings(html, venueCinergia, day).map { it.detailsUrl }).isEqualTo(listOf<String?>("https://x.18tickets.it/film/91221", "https://x.18tickets.it/film/2"))
    }

    @Test
    fun eachFilmKeepsItsOwnPageEvenWhenThePreviousBlockLinksToItsFilm() {
        val html = film(1, "THE SOCIAL", "01/10/2026", "18:30") +
            "<a href='https://x.18tickets.it/film/1?ref_date=2026-10-01'>Prenota</a>" +
            film(2, "UNA STORIA", "01/10/2026", "21:00")

        val showings = parseEighteenTicketsShowings(html, venueCinergia, day)

        assertThat(showings.associate { it.title to it.detailsUrl }).isEqualTo(mapOf("The Social" to "https://x.18tickets.it/film/1", "Una Storia" to "https://x.18tickets.it/film/2"))
    }
}

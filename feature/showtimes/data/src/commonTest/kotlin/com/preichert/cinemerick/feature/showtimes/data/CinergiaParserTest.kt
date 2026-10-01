package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CinergiaParserTest {

    private val day = LocalDate(2026, 10, 1)

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
        ${times.joinToString("\n") { "<a data-time='1' href='https://x/film/$id/s#theater-init'>\n<li class='btn'>\n<i class='fas fa-ticket-alt'></i>\n$it\n</li>\n</a>" }}
        </ul>
        </div>
    """.trimIndent()

    @Test
    fun parsesFilmsTimesAndPoster() {
        val html = film(1, "VERITY", "01/10/2026", "18:30", "21:00") + film(2, "(V.O.S.) DIGGER", "01/10/2026", "9:05")

        val showings = parseCinergiaShowings(html, day)

        assertEquals(listOf("Verity", "Verity", "Digger"), showings.map { it.title })
        assertEquals(listOf(LocalTime(18, 30), LocalTime(21, 0), LocalTime(9, 5)), showings.map { it.time })
        assertEquals(listOf(null, null, "VO"), showings.map { it.format })
        assertEquals("https://cdn.18tickets.net/p/1.jpg", showings.first().posterUrl)
        assertEquals(setOf(Cinema.CINERGIA), showings.map { it.cinema }.toSet())
    }

    @Test
    fun parsesTheEscapedScriptResponse() {
        val html = film(1, "VERITY", "01/10/2026", "18:30")
        val script = "\$(\"#movie-list\").html('" + html.replace("'", "\\'").replace("\n", "\\n").replace("</", "<\\/") + "');"

        assertEquals(listOf(LocalTime(18, 30)), parseCinergiaShowings(script, day).map { it.time })
    }

    @Test
    fun ignoresScheduleForAnotherDay() {
        assertEquals(emptyList(), parseCinergiaShowings(film(1, "VERITY", "02/10/2026", "18:30"), day))
    }
}

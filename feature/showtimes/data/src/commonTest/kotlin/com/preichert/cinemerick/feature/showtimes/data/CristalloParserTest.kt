package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CristalloParserTest {

    private val day = LocalDate(2026, 10, 4)

    private fun film(title: String, poster: String, vararg times: String) = """
        <div class="amy-movie-item">
            <div class="amy-movie-item-inner">
                <div class="amy-movie-item-front"><div class="amy-movie-item-poster">
                    <a href="https://x/movie/a/"><img class="" src="$poster" alt="$title"/></a>
                </div></div>
                <div class="amy-movie-item-back"><div class="amy-movie-item-content">
                    <h3 class="amy-movie-field-title"><a href="https://x/movie/a/">$title</a></h3>
                    <div class="amy-movie-item-meta"><span class="amy-movie-field-duration">01 ore 59 minuti</span></div>
                </div></div>
                <div class="amy-movie-item-showtimes amy-item-1">
                    <div class="amy-movie-intro-times">
                    ${times.joinToString("\n") { "<span>$it</span>" }}
                    <a class="button" href="https://www.webtic.it/"><i class="fa fas fa-ticket-alt"></i> Acquista il biglietto</a>
                    </div>
                </div>
            </div>
        </div>
    """.trimIndent()

    @Test
    fun parsesFilmsTimesAndPoster() {
        val html = film("DIGGER", "https://x/digger.jpg", "17.00", "21.15") + film("CARAVAGGIO", "https://x/car.jpg", "19.00")

        val showings = parseCristalloShowings(html, day)

        assertEquals(listOf("Digger", "Digger", "Caravaggio"), showings.map { it.title })
        assertEquals(listOf(LocalTime(17, 0), LocalTime(21, 15), LocalTime(19, 0)), showings.map { it.time })
        assertEquals("https://x/digger.jpg", showings.first().posterUrl)
        assertEquals(setOf(day), showings.map { it.day }.toSet())
        assertEquals(setOf(Cinema.CRISTALLO), showings.map { it.cinema }.toSet())
    }

    @Test
    fun readsTimesWithLeadingSpaceAndEntranceNote() {
        val html = film("DIGGER", "https://x/d.jpg", "12.30 INGRESSO 5€", " 14.45", "11.00 - INGRESSO 5€")

        assertEquals(listOf(LocalTime(12, 30), LocalTime(14, 45), LocalTime(11, 0)), parseCristalloShowings(html, day).map { it.time })
    }

    @Test
    fun unescapesTitleEntities() {
        val html = film("SANTIAGO &#8211; UN CAMMINO", "https://x/s.jpg", "20.30") + film("L&#8217;ISOLA DEI RICORDI", "https://x/i.jpg", "18.00")

        assertEquals(listOf("Santiago – Un Cammino", "L’Isola Dei Ricordi"), parseCristalloShowings(html, day).map { it.title })
    }

    @Test
    fun parsesTheJsonStringResponse() {
        val html = film("DIGGER", "https://x/d.jpg", "17.00")
        val json = "\"" + html.replace("\\", "\\\\").replace("\"", "\\\"").replace("/", "\\/").replace("\n", "\\r\\n") + "\""

        assertEquals(listOf(LocalTime(17, 0)), parseCristalloShowings(json, day).map { it.time })
    }

    @Test
    fun returnsNothingForAnEmptyDay() {
        assertEquals(emptyList(), parseCristalloShowings("\"\"", day))
    }
}

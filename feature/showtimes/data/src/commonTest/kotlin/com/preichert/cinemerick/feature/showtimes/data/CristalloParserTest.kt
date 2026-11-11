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

class CristalloParserTest {

    private val day = LocalDate(2026, 10, 4)
    private val venueCristallo = Venue(Chain.CRISTALLO, "oderzo", "Oderzo", null, null)

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

        val showings = parseCristalloShowings(html, venueCristallo, day)

        assertThat(showings.map { it.title }).isEqualTo(listOf("Digger", "Digger", "Caravaggio"))
        assertThat(showings.map { it.time }).isEqualTo(listOf(LocalTime(17, 0), LocalTime(21, 15), LocalTime(19, 0)))
        assertThat(showings.first().posterUrl).isEqualTo("https://x/digger.jpg")
        assertThat(showings.map { it.day }.toSet()).isEqualTo(setOf(day))
        assertThat(showings.map { it.venue }.toSet()).isEqualTo(setOf(venueCristallo))
        assertThat(showings.any { it.soldOut }).isFalse()
    }

    @Test
    fun readsTimesWithLeadingSpaceAndEntranceNote() {
        val html = film("DIGGER", "https://x/d.jpg", "12.30 INGRESSO 5€", " 14.45", "11.00 - INGRESSO 5€")

        assertThat(parseCristalloShowings(html, venueCristallo, day).map { it.time }).isEqualTo(listOf(LocalTime(12, 30), LocalTime(14, 45), LocalTime(11, 0)))
    }

    @Test
    fun unescapesTitleEntities() {
        val html = film("SANTIAGO &#8211; UN CAMMINO", "https://x/s.jpg", "20.30") + film("L&#8217;ISOLA DEI RICORDI", "https://x/i.jpg", "18.00")

        assertThat(parseCristalloShowings(html, venueCristallo, day).map { it.title }).isEqualTo(listOf("Santiago – Un Cammino", "L’Isola Dei Ricordi"))
    }

    @Test
    fun parsesTheJsonStringResponse() {
        val html = film("DIGGER", "https://x/d.jpg", "17.00")
        val json = "\"" + html.replace("\\", "\\\\").replace("\"", "\\\"").replace("/", "\\/").replace("\n", "\\r\\n") + "\""

        assertThat(parseCristalloShowings(json, venueCristallo, day).map { it.time }).isEqualTo(listOf(LocalTime(17, 0)))
    }

    @Test
    fun returnsNothingForAnEmptyDay() {
        assertThat(parseCristalloShowings("\"\"", venueCristallo, day)).isEmpty()
    }

    @Test
    fun linksEachFilmToItsFilmPage() {
        val html = film("DIGGER", "https://x/d.jpg", "17.00", "21.15")

        assertThat(parseCristalloShowings(html, venueCristallo, day).map { it.detailsUrl }).isEqualTo(listOf<String?>("https://x/movie/a/", "https://x/movie/a/"))
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull

class NotoriousParserTest {

    private val thursday = LocalDate(2026, 10, 1)
    private val friday = LocalDate(2026, 10, 2)
    private val venueFerrara = Venue(Chain.NOTORIOUS, "ferrara", "Ferrara", null, null)

    private val cinemaSpan = "<span class=\"cinema\" id=\"5848\" style=\"display:none;\"></span>"

    private fun film(id: Int, title: String, vararg days: Pair<String, List<String>>) = """
        <div class="filmContainer oddFilm">
        <div class="locandina">
        <img src="../cvu/modules/img_switcher.php?idfilm=$id&type=446&ext=jpg" idFilm="$id" alt="x"/>
        </div>
        <div class="datiFilm">
        <div class="titolo">$title</div>
        <div class="scheda" idfilm="$id"><i class="fas fa-angle-right"></i>visualizza la scheda</div>
        </div>
        <ul class="orari">
        ${days.joinToString("\n") { (day, times) ->
            "<li class=\"hours oddHour pngFixed\"><div class=\"dayName\">$day</div>" +
                times.withIndex().joinToString("") { (index, time) -> "<span class=\"orario_\" idcalend=\"${id}0$index\">$time</span>" } + "</li>"
        }}
        </ul>
        </div>
    """.trimIndent()

    @Test
    fun parsesTitleFormatPosterAndTimes() {
        val html = film(1, "<span class=\"newMovie\">NEW! </span>DIGGER [2026] | ORIGINAL VERSION", "Gioved&igrave; 01/10" to listOf("20:05", "9:30")) +
            film(2, "LA CITT&Agrave; DEI VIVI", "Venerd&igrave; 02/10" to listOf("21:00"))

        val showings = parseNotoriousShowings(html, venueFerrara, listOf(thursday, friday))

        assertThat(showings.map { it.title }).isEqualTo(listOf("Digger", "Digger", "La Città Dei Vivi"))
        assertThat(showings.map { it.day }).isEqualTo(listOf(thursday, thursday, friday))
        assertThat(showings.map { it.time }).isEqualTo(listOf(LocalTime(20, 5), LocalTime(9, 30), LocalTime(21, 0)))
        assertThat(showings.map { it.format }).isEqualTo(listOf("VO", "VO", null))
        assertThat(showings.first().posterUrl).isEqualTo("https://www.notoriouscinemas.it/cvu/modules/img_switcher.php?idfilm=1&type=446&ext=jpg")
        assertThat(showings.map { it.venue }.toSet()).isEqualTo(setOf(venueFerrara))
        assertThat(showings.any { it.soldOut }).isFalse()
    }

    @Test
    fun ignoresDaysThatWereNotRequested() {
        val html = film(1, "BEAST [2026]", "Gioved&igrave; 01/10" to listOf("20:05"), "Venerd&igrave; 02/10" to listOf("20:05"))

        assertThat(parseNotoriousShowings(html, venueFerrara, listOf(friday)).map { it.day }).isEqualTo(listOf(friday))
    }

    @Test
    fun buildsPurchaseUrlFromCinemaIdAndCalendarId() {
        val html = cinemaSpan + film(1, "DIGGER [2026]", "Gioved&igrave; 01/10" to listOf("20:05", "22:30")) +
            film(2, "BEAST [2026]", "Gioved&igrave; 01/10" to listOf("21:00"))

        val showings = parseNotoriousShowings(html, venueFerrara, listOf(thursday))

        assertThat(showings.map { it.ticketUrl }).isEqualTo(listOf(
                "https://www.notoriouscinemas.it/generic/seatsframe.php?sc=5848&sp=100",
                "https://www.notoriouscinemas.it/generic/seatsframe.php?sc=5848&sp=101",
                "https://www.notoriouscinemas.it/generic/seatsframe.php?sc=5848&sp=200"
            ))
    }

    @Test
    fun pageWithoutCinemaIdGivesNoPurchaseUrl() {
        val html = film(1, "DIGGER [2026]", "Gioved&igrave; 01/10" to listOf("20:05"))

        assertThat(parseNotoriousShowings(html, venueFerrara, listOf(thursday)).map { it.ticketUrl }).isEqualTo(listOf<String?>(null))
    }

    @Test
    fun linksEachFilmToItsFilmPage() {
        val html = film(48721, "DIGGER", "Gioved&igrave; 01/10" to listOf("20:05")) + film(48720, "BEAST", "Gioved&igrave; 01/10" to listOf("21:00"))

        val showings = parseNotoriousShowings(html, venueFerrara, listOf(thursday))

        assertThat(showings.map { it.detailsUrl }).isEqualTo(listOf("https://www.notoriouscinemas.it/generic/scheda.php?id=48721", "https://www.notoriouscinemas.it/generic/scheda.php?id=48720"))
    }

    @Test
    fun listsVenuesFromHomepageIncludingComingSoonOnes() {
        val html = """
            <li><a href="../ferrara/index.php">Ferrara</a></li>
            <li><a href="../fiumeveneto/index.php"><span style="color: #f00;">Coming soon!</span> Fiume Veneto</a></li>
            <li><a href="../sinalunga/index.php">Sinalunga</a></li>
        """.trimIndent()

        val venues = parseNotoriousVenues(html)

        assertThat(venues.map { it.id }).isEqualTo(listOf("ferrara", "fiumeveneto", "sinalunga"))
        assertThat(venues.map { it.name }).isEqualTo(listOf("Ferrara", "Fiume Veneto", "Sinalunga"))
        assertThat(venues[1].webUrl).isEqualTo("https://www.notoriouscinemas.it/fiumeveneto/index.php")
    }

    @Test
    fun decodesHtmlEntitiesInVenueNames() {
        val html = "<li><a href=\"../fiumeveneto/index.php\">Fiume&nbsp;Veneto &amp; Sacile</a></li>"

        assertThat(parseNotoriousVenues(html).single().name).isEqualTo("Fiume Veneto & Sacile")
    }

    @Test
    fun filmWithoutFilmPageLinkHasNoDetailsUrl() {
        val html = film(1, "DIGGER", "Gioved&igrave; 01/10" to listOf("20:05")).replace(Regex("<div class=\"scheda\"[^>]*>.*?</div>"), "")

        assertThat(parseNotoriousShowings(html, venueFerrara, listOf(thursday)).single().detailsUrl).isNull()
    }
}

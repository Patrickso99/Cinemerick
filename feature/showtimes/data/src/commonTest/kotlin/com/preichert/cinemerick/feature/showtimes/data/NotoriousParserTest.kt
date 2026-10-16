package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

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

        assertEquals(listOf("Digger", "Digger", "La Città Dei Vivi"), showings.map { it.title })
        assertEquals(listOf(thursday, thursday, friday), showings.map { it.day })
        assertEquals(listOf(LocalTime(20, 5), LocalTime(9, 30), LocalTime(21, 0)), showings.map { it.time })
        assertEquals(listOf("VO", "VO", null), showings.map { it.format })
        assertEquals("https://www.notoriouscinemas.it/cvu/modules/img_switcher.php?idfilm=1&type=446&ext=jpg", showings.first().posterUrl)
        assertEquals(setOf(venueFerrara), showings.map { it.venue }.toSet())
        assertFalse(showings.any { it.soldOut })
    }

    @Test
    fun ignoresDaysThatWereNotRequested() {
        val html = film(1, "BEAST [2026]", "Gioved&igrave; 01/10" to listOf("20:05"), "Venerd&igrave; 02/10" to listOf("20:05"))

        assertEquals(listOf(friday), parseNotoriousShowings(html, venueFerrara, listOf(friday)).map { it.day })
    }

    @Test
    fun buildsPurchaseUrlFromCinemaIdAndCalendarId() {
        val html = cinemaSpan + film(1, "DIGGER [2026]", "Gioved&igrave; 01/10" to listOf("20:05", "22:30")) +
            film(2, "BEAST [2026]", "Gioved&igrave; 01/10" to listOf("21:00"))

        val showings = parseNotoriousShowings(html, venueFerrara, listOf(thursday))

        assertEquals(
            listOf(
                "https://www.notoriouscinemas.it/generic/seatsframe.php?sc=5848&sp=100",
                "https://www.notoriouscinemas.it/generic/seatsframe.php?sc=5848&sp=101",
                "https://www.notoriouscinemas.it/generic/seatsframe.php?sc=5848&sp=200"
            ),
            showings.map { it.ticketUrl }
        )
    }

    @Test
    fun pageWithoutCinemaIdGivesNoPurchaseUrl() {
        val html = film(1, "DIGGER [2026]", "Gioved&igrave; 01/10" to listOf("20:05"))

        assertEquals(listOf<String?>(null), parseNotoriousShowings(html, venueFerrara, listOf(thursday)).map { it.ticketUrl })
    }

    @Test
    fun linksEachFilmToItsFilmPage() {
        val html = film(48721, "DIGGER", "Gioved&igrave; 01/10" to listOf("20:05")) + film(48720, "BEAST", "Gioved&igrave; 01/10" to listOf("21:00"))

        val showings = parseNotoriousShowings(html, venueFerrara, listOf(thursday))

        assertEquals(
            listOf("https://www.notoriouscinemas.it/generic/scheda.php?id=48721", "https://www.notoriouscinemas.it/generic/scheda.php?id=48720"),
            showings.map { it.detailsUrl }
        )
    }

    @Test
    fun listsVenuesFromHomepageIncludingComingSoonOnes() {
        val html = """
            <li><a href="../ferrara/index.php">Ferrara</a></li>
            <li><a href="../fiumeveneto/index.php"><span style="color: #f00;">Coming soon!</span> Fiume Veneto</a></li>
            <li><a href="../sinalunga/index.php">Sinalunga</a></li>
        """.trimIndent()

        val venues = parseNotoriousVenues(html)

        assertEquals(listOf("ferrara", "fiumeveneto", "sinalunga"), venues.map { it.id })
        assertEquals(listOf("Ferrara", "Fiume Veneto", "Sinalunga"), venues.map { it.name })
        assertEquals("https://www.notoriouscinemas.it/fiumeveneto/index.php", venues[1].webUrl)
    }

    @Test
    fun decodesHtmlEntitiesInVenueNames() {
        val html = "<li><a href=\"../fiumeveneto/index.php\">Fiume&nbsp;Veneto &amp; Sacile</a></li>"

        assertEquals("Fiume Veneto & Sacile", parseNotoriousVenues(html).single().name)
    }

    @Test
    fun filmWithoutFilmPageLinkHasNoDetailsUrl() {
        val html = film(1, "DIGGER", "Gioved&igrave; 01/10" to listOf("20:05")).replace(Regex("<div class=\"scheda\"[^>]*>.*?</div>"), "")

        assertNull(parseNotoriousShowings(html, venueFerrara, listOf(thursday)).single().detailsUrl)
    }
}

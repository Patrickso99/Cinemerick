package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArcadiaParserTest {

    private val tuesday = LocalDate(2026, 12, 15)
    private val wednesday = LocalDate(2026, 12, 16)
    private val venueMelzo = Venue(Chain.ARCADIA, "melzo", "Melzo", "Lombardia", null)

    private fun film(title: String, poster: String, vararg days: Pair<String, List<Pair<String, String?>>>) = """
        <div class="filmContainer" data-ts_rel="1797375600">
            <div class="locandina"><img src="$poster" alt="$title | INFINITY VISION"/></div>
            <div class="datiFilm"><div class="titolo"><h1>$title</h1></div></div>
            <ul class="orari">
            ${days.joinToString("\n") { (heading, times) ->
                """<li class="hours">
                    <div class="dayName">
                    $heading </div>
                    <div class="orari_cont">
                    ${times.joinToString("\n") { (time, version) ->
                        """<div class="orario">
                            <a href="/acquista/x/5085/4923/${time.replace(":", "")}/" title="$title" class="link_acquista">
                                $time </a>
                            ${version?.let { """<div class="versione">$it</div>""" } ?: ""}
                        </div>"""
                    }}
                    </div>
                </li>"""
            }}
            </ul>
        </div>
    """.trimIndent()

    @Test
    fun parsesFilmsDaysTimesFormatAndPoster() {
        val html = film(
            "AVENGERS - DOOMSDAY", "/cdn/movies/a.jpg",
            "Martedì 15/12" to listOf("00:01" to "INFINITY VISION", "21:30" to null),
            "Mercoledì 16/12" to listOf("09:30" to "ENERGIA INFINITY VISION")
        ) + film("DUNE", "https://x/dune.jpg", "Martedì 15/12" to listOf("18:00" to null))

        val showings = parseArcadiaShowings(html, venueMelzo, listOf(tuesday, wednesday))

        assertEquals(4, showings.size)
        val first = showings.first()
        assertEquals("Avengers - Doomsday", first.title)
        assertEquals(tuesday, first.day)
        assertEquals(LocalTime(0, 1), first.time)
        assertEquals("INFINITY VISION", first.format)
        assertEquals("https://www.arcadiacinema.com/cdn/movies/a.jpg", first.posterUrl)
        assertEquals(null, showings[1].format)
        assertEquals("ENERGIA INFINITY VISION", showings[2].format)
        assertEquals("https://x/dune.jpg", showings[3].posterUrl)
        assertFalse(showings.any { it.soldOut })
    }

    @Test
    fun eachTimeKeepsItsOwnAbsolutePurchaseUrl() {
        val html = film(
            "AVENGERS - DOOMSDAY", "/p.jpg",
            "Martedì 15/12" to listOf("00:01" to "INFINITY VISION", "21:30" to null),
            "Mercoledì 16/12" to listOf("09:30" to null)
        )

        val showings = parseArcadiaShowings(html, venueMelzo, listOf(tuesday, wednesday))

        assertEquals(
            listOf(
                "https://www.arcadiacinema.com/acquista/x/5085/4923/0001/",
                "https://www.arcadiacinema.com/acquista/x/5085/4923/2130/",
                "https://www.arcadiacinema.com/acquista/x/5085/4923/0930/"
            ),
            showings.map { it.ticketUrl }
        )
    }

    @Test
    fun purchaseUrlHasHtmlEntitiesUnescaped() {
        val html = film("DUNE", "/p.jpg", "Martedì 15/12" to listOf("18:00" to null))
            .replace("/acquista/x/5085/4923/1800/", "/acquista/x?a=1&amp;b=2")

        assertEquals(
            listOf("https://www.arcadiacinema.com/acquista/x?a=1&b=2"),
            parseArcadiaShowings(html, venueMelzo, listOf(tuesday)).map { it.ticketUrl }
        )
    }

    @Test
    fun ignoresDaysThatWereNotRequested() {
        val html = film("DUNE", "/p.jpg", "Martedì 15/12" to listOf("18:00" to null), "Mercoledì 16/12" to listOf("20:00" to null))

        val showings = parseArcadiaShowings(html, venueMelzo, listOf(wednesday))

        assertEquals(listOf(LocalTime(20, 0)), showings.map { it.time })
    }

    @Test
    fun emptyPageHasNoShowings() {
        assertTrue(parseArcadiaShowings("<html></html>", venueMelzo, listOf(tuesday)).isEmpty())
    }

    @Test
    fun parsesVenuesFromMenuWithoutDuplicates() {
        val menu = """<a href="/melzo" title="Melzo">Melzo</a><a href="/bellinzago" title="Bellinzago Lombardo">Bellinzago Lombardo</a>"""

        val venues = parseArcadiaVenues(menu + menu)

        assertEquals(listOf("melzo", "bellinzago"), venues.map { it.id })
        assertEquals(listOf("Melzo", "Bellinzago Lombardo"), venues.map { it.name })
        assertEquals("https://www.arcadiacinema.com/melzo", venues.first().webUrl)
    }
}

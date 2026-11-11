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
import assertk.assertions.isNull

class ArcadiaParserTest {

    private val tuesday = LocalDate(2026, 12, 15)
    private val wednesday = LocalDate(2026, 12, 16)
    private val venueMelzo = Venue(Chain.ARCADIA, "melzo", "Melzo", "Lombardia", null)

    private fun film(title: String, poster: String, vararg days: Pair<String, List<Pair<String, String?>>>) = """
        <div class="filmContainer" data-ts_rel="1797375600">
            <div class="locandina"><img src="$poster" alt="$title | INFINITY VISION"/></div>
            <div class="datiFilm"><div class="titolo"><h1>$title</h1></div>
                <a href="/scheda/${title.lowercase().replace(" ", "-")}/48783/3364/"><div class="btt_scheda">Scheda film</div></a>
            </div>
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

        assertThat(showings.size).isEqualTo(4)
        val first = showings.first()
        assertThat(first.title).isEqualTo("Avengers - Doomsday")
        assertThat(first.day).isEqualTo(tuesday)
        assertThat(first.time).isEqualTo(LocalTime(0, 1))
        assertThat(first.format).isEqualTo("INFINITY VISION")
        assertThat(first.posterUrl).isEqualTo("https://www.arcadiacinema.com/cdn/movies/a.jpg")
        assertThat(showings[1].format).isNull()
        assertThat(showings[2].format).isEqualTo("ENERGIA INFINITY VISION")
        assertThat(showings[3].posterUrl).isEqualTo("https://x/dune.jpg")
        assertThat(showings.any { it.soldOut }).isFalse()
    }

    @Test
    fun eachTimeKeepsItsOwnAbsolutePurchaseUrl() {
        val html = film(
            "AVENGERS - DOOMSDAY", "/p.jpg",
            "Martedì 15/12" to listOf("00:01" to "INFINITY VISION", "21:30" to null),
            "Mercoledì 16/12" to listOf("09:30" to null)
        )

        val showings = parseArcadiaShowings(html, venueMelzo, listOf(tuesday, wednesday))

        assertThat(showings.map { it.ticketUrl }).isEqualTo(listOf(
                "https://www.arcadiacinema.com/acquista/x/5085/4923/0001/",
                "https://www.arcadiacinema.com/acquista/x/5085/4923/2130/",
                "https://www.arcadiacinema.com/acquista/x/5085/4923/0930/"
            ))
    }

    @Test
    fun purchaseUrlHasHtmlEntitiesUnescaped() {
        val html = film("DUNE", "/p.jpg", "Martedì 15/12" to listOf("18:00" to null))
            .replace("/acquista/x/5085/4923/1800/", "/acquista/x?a=1&amp;b=2")

        assertThat(parseArcadiaShowings(html, venueMelzo, listOf(tuesday)).map { it.ticketUrl }).isEqualTo(listOf("https://www.arcadiacinema.com/acquista/x?a=1&b=2"))
    }

    @Test
    fun ignoresDaysThatWereNotRequested() {
        val html = film("DUNE", "/p.jpg", "Martedì 15/12" to listOf("18:00" to null), "Mercoledì 16/12" to listOf("20:00" to null))

        val showings = parseArcadiaShowings(html, venueMelzo, listOf(wednesday))

        assertThat(showings.map { it.time }).isEqualTo(listOf(LocalTime(20, 0)))
    }

    @Test
    fun emptyPageHasNoShowings() {
        assertThat(parseArcadiaShowings("<html></html>", venueMelzo, listOf(tuesday))).isEmpty()
    }

    @Test
    fun parsesVenuesFromMenuWithoutDuplicates() {
        val menu = """<a href="/melzo" title="Melzo">Melzo</a><a href="/bellinzago" title="Bellinzago Lombardo">Bellinzago Lombardo</a>"""

        val venues = parseArcadiaVenues(menu + menu)

        assertThat(venues.map { it.id }).isEqualTo(listOf("melzo", "bellinzago"))
        assertThat(venues.map { it.name }).isEqualTo(listOf("Melzo", "Bellinzago Lombardo"))
        assertThat(venues.first().webUrl).isEqualTo("https://www.arcadiacinema.com/melzo")
    }

    @Test
    fun linksEachFilmToItsAbsoluteFilmPage() {
        val html = film("DUNE", "/p.jpg", "Martedì 15/12" to listOf("18:00" to null))

        assertThat(parseArcadiaShowings(html, venueMelzo, listOf(tuesday)).single().detailsUrl).isEqualTo("https://www.arcadiacinema.com/scheda/dune/48783/3364/")
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DonBoscoParserTest {

    private val monday = LocalDate(2026, 10, 5)
    private val tuesday = LocalDate(2026, 10, 6)
    private val venueDonBosco = Venue(Chain.DON_BOSCO, "san-dona-di-piave", "San Donà di Piave", null, null)

    private fun film(id: Int, title: String, poster: String, vararg days: Pair<String, List<String>>) = """
        <article id="film-$id" class="relative scroll-mt-20">
            <div><img src="$poster" alt="Poster $title" class="max-h-[65vh]" loading="lazy"></div>
            <div class="space-y-4">
            ${days.joinToString("\n") { (heading, times) ->
                """<div class="text-center">
                    <div class="giorno text-lg font-semibold uppercase">$heading</div>
                    <div class="orari mt-2 flex">
                    ${times.joinToString("\n") { """<span class="group"><span class="cinema-promo-time">$it</span></span>""" }}
                    </div>
                </div>"""
            }}
            </div>
        </article>
    """.trimIndent()

    // Adds the film's "Acquista i biglietti online" button, which sells through Cinebot.
    private fun String.withOnlineButton(titleId: Int) = replace(
        "</article>",
        """<a href="https://ticket.cinebot.it/donbosco2/titolo/$titleId" target="_blank" class="cinema-btn-primary">Acquista i biglietti online</a></article>"""
    )

    @Test
    fun parsesFilmsDaysTimesAndPoster() {
        val html = film(28, "Caravaggio", "https://x/car.jpeg", "LUNEDÌ 5 OTTOBRE" to listOf("16.30", "21.00"), "MARTEDÌ 6 OTTOBRE" to listOf("15.00")) +
            film(29, "Digger", "https://x/digger.jpg", "LUNEDÌ 5 OTTOBRE" to listOf("19.00"))

        val showings = parseDonBoscoShowings(html, venueDonBosco, listOf(monday, tuesday))

        assertEquals(4, showings.size)
        assertEquals(
            listOf(LocalTime(16, 30), LocalTime(21, 0)),
            showings.filter { it.title == "Caravaggio" && it.day == monday }.map { it.time }
        )
        assertEquals(LocalTime(15, 0), showings.single { it.title == "Caravaggio" && it.day == tuesday }.time)
        assertEquals("https://x/digger.jpg", showings.single { it.title == "Digger" }.posterUrl)
        assertFalse(showings.any { it.soldOut })
    }

    @Test
    fun ignoresDaysThatWereNotRequested() {
        val html = film(28, "Caravaggio", "https://x/car.jpeg", "LUNEDÌ 5 OTTOBRE" to listOf("16.30"), "MARTEDÌ 6 OTTOBRE" to listOf("15.00"))

        val showings = parseDonBoscoShowings(html, venueDonBosco, listOf(tuesday))

        assertEquals(listOf(LocalTime(15, 0)), showings.map { it.time })
    }

    @Test
    fun filmWithoutShowtimesYieldsNothing() {
        val html = film(30, "Prossimamente", "https://x/p.jpg")

        assertTrue(parseDonBoscoShowings(html, venueDonBosco, listOf(monday)).isEmpty())
    }

    @Test
    fun filmWithOnlineButtonLinksEveryShowingToItsFilmPage() {
        val html = film(28, "Caravaggio", "https://x/car.jpeg", "LUNEDÌ 5 OTTOBRE" to listOf("16.30", "21.00"), "MARTEDÌ 6 OTTOBRE" to listOf("15.00"))
            .withOnlineButton(671)

        val showings = parseDonBoscoShowings(html, venueDonBosco, listOf(monday, tuesday))

        assertEquals(3, showings.size)
        assertEquals(List(3) { "https://ticket.cinebot.it/donbosco2/titolo/671" }, showings.map { it.ticketUrl })
    }

    @Test
    fun filmWithoutOnlineButtonHasNoPurchaseUrlAndDoesNotBorrowTheNextFilmsLink() {
        val html = film(28, "Caravaggio", "https://x/car.jpeg", "LUNEDÌ 5 OTTOBRE" to listOf("16.30")) +
            film(29, "Digger", "https://x/digger.jpg", "LUNEDÌ 5 OTTOBRE" to listOf("19.00")).withOnlineButton(673)

        val showings = parseDonBoscoShowings(html, venueDonBosco, listOf(monday))

        assertEquals(null, showings.single { it.title == "Caravaggio" }.ticketUrl)
        assertEquals("https://ticket.cinebot.it/donbosco2/titolo/673", showings.single { it.title == "Digger" }.ticketUrl)
    }

    @Test
    fun readsTimesWhateverTheirSpanClass() {
        val html = film(28, "Naza", "https://x/naza.jpg", "LUNEDÌ 5 OTTOBRE" to listOf("17.20"))
            .replace("""<span class="cinema-promo-time">17.20</span></span>""", """<span class="cinema-promo-time">17.20</span></span><span class="group"><span class="">21.00</span></span>""")

        val showings = parseDonBoscoShowings(html, venueDonBosco, listOf(monday))

        assertEquals(listOf(LocalTime(17, 20), LocalTime(21, 0)), showings.map { it.time })
    }

    @Test
    fun filmWhoseTimesAllHaveEmptyClassIsNotLost() {
        val html = """
            <article id="film-30" class="relative">
                <div><img src="https://x/c.jpg" alt="Poster Carlo Acutis" loading="lazy"></div>
                <div class="giorno text-lg">LUNEDÌ 5 OTTOBRE</div>
                <div class="orari"><span class="group"><span class="">15.30</span></span><span class="group"><span class="">18.00</span></span></div>
            </article>
        """.trimIndent()

        val showings = parseDonBoscoShowings(html, venueDonBosco, listOf(monday))

        assertEquals(listOf("Carlo Acutis", "Carlo Acutis"), showings.map { it.title })
        assertEquals(listOf(LocalTime(15, 30), LocalTime(18, 0)), showings.map { it.time })
    }
}

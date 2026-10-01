package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class NotoriousParserTest {

    private val thursday = LocalDate(2026, 10, 1)
    private val friday = LocalDate(2026, 10, 2)

    private fun film(id: Int, title: String, vararg days: Pair<String, List<String>>) = """
        <div class="filmContainer oddFilm">
        <div class="locandina">
        <img src="../cvu/modules/img_switcher.php?idfilm=$id&type=446&ext=jpg" idFilm="$id" alt="x"/>
        </div>
        <div class="datiFilm">
        <div class="titolo">$title</div>
        </div>
        <ul class="orari">
        ${days.joinToString("\n") { (day, times) ->
            "<li class=\"hours oddHour pngFixed\"><div class=\"dayName\">$day</div>" +
                times.joinToString("") { "<span class=\"orario_\" idcalend=\"1\">$it</span>" } + "</li>"
        }}
        </ul>
        </div>
    """.trimIndent()

    @Test
    fun parsesTitleFormatPosterAndTimes() {
        val html = film(1, "<span class=\"newMovie\">NEW! </span>DIGGER [2026] | ORIGINAL VERSION", "Gioved&igrave; 01/10" to listOf("20:05", "9:30")) +
            film(2, "LA CITT&Agrave; DEI VIVI", "Venerd&igrave; 02/10" to listOf("21:00"))

        val showings = parseNotoriousShowings(html, listOf(thursday, friday))

        assertEquals(listOf("Digger", "Digger", "La Città Dei Vivi"), showings.map { it.title })
        assertEquals(listOf(thursday, thursday, friday), showings.map { it.day })
        assertEquals(listOf(LocalTime(20, 5), LocalTime(9, 30), LocalTime(21, 0)), showings.map { it.time })
        assertEquals(listOf("VO", "VO", null), showings.map { it.format })
        assertEquals("https://www.notoriouscinemas.it/cvu/modules/img_switcher.php?idfilm=1&type=446&ext=jpg", showings.first().posterUrl)
        assertEquals(setOf(Cinema.NOTORIOUS), showings.map { it.cinema }.toSet())
    }

    @Test
    fun ignoresDaysThatWereNotRequested() {
        val html = film(1, "BEAST [2026]", "Gioved&igrave; 01/10" to listOf("20:05"), "Venerd&igrave; 02/10" to listOf("20:05"))

        assertEquals(listOf(friday), parseNotoriousShowings(html, listOf(friday)).map { it.day })
    }
}

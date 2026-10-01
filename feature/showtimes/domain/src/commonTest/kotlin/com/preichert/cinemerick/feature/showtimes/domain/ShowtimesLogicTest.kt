package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ShowtimesLogicTest {

    private val thursday = LocalDate(2026, 10, 1)
    private val friday = LocalDate(2026, 10, 2)
    private val farPast = LocalDateTime(2026, 9, 1, 0, 0)

    private fun showing(title: String, day: LocalDate, time: String, cinema: Cinema) =
        Showing(title, day, LocalTime.parse(time), cinema)

    @Test
    fun sameFilmFromBothCinemasIsGroupedWithShortestTitleAsHeader() {
        val groups = listOf(
            showing("HEART OF THE BEAST - NEL PROFONDO SELVAGGIO", thursday, "21:40", Cinema.THE_SPACE),
            showing("Heart of the Beast", thursday, "21:40", Cinema.UCI),
            showing("Naza", friday, "21:40", Cinema.THE_SPACE),
            showing("Naza C.A.", friday, "19:40", Cinema.UCI)
        ).groupByFilm()

        // Group titles are the shortest originals
        assertEquals(listOf("Heart of the Beast", "Naza"), groups.map { it.title })
        // Original titles are preserved in showings (not transformed)
        assertEquals(
            listOf("Heart of the Beast", "HEART OF THE BEAST - NEL PROFONDO SELVAGGIO"),
            groups.first().showings.map { it.title }
        )
        assertEquals(
            listOf("Naza C.A.", "Naza"),
            groups.last().showings.map { it.title }
        )
        // Cinema and time ordering is preserved
        assertEquals(
            listOf(Cinema.UCI, Cinema.THE_SPACE),
            groups.first().showings.map { it.cinema }
        )
        assertEquals(
            listOf(LocalTime.parse("19:40"), LocalTime.parse("21:40")),
            groups.last().showings.map { it.time }
        )
    }

    @Test
    fun pollTextUsesGroupTitleForAllCinemas() {
        val text = listOf(
            showing("Heart Of The Beast - Nel Profondo Selvaggio", thursday, "21:40", Cinema.THE_SPACE),
            showing("Heart of the Beast", thursday, "21:40", Cinema.UCI)
        ).groupByFilm().toPollText()

        assertEquals(
            "Heart of the Beast (Giovedì - 21:40 - Marcon)\nHeart of the Beast (Giovedì - 21:40 - Silea)",
            text
        )
    }

    private fun validTime(text: String): LocalTime? =
        (parseTimeInput(text) as ParsedTime.Valid).value

    @Test
    fun parseTimeInputAcceptsHourOnlyAndFullTimes() {
        assertEquals(null, validTime(""))
        assertEquals(null, validTime("   "))
        assertEquals(LocalTime(21, 0), validTime("21"))
        assertEquals(LocalTime(9, 0), validTime("9"))
        assertEquals(LocalTime(0, 0), validTime("0"))
        assertEquals(LocalTime(9, 30), validTime("9:30"))
        assertEquals(LocalTime(21, 15), validTime("21:15"))
        assertEquals(LocalTime(21, 15), validTime(" 21:15 "))
    }

    @Test
    fun parseTimeInputMapsTwentyFourToLastMinuteOfDay() {
        assertEquals(LocalTime(23, 59), validTime("24"))
        assertEquals(LocalTime(23, 59), validTime("24:00"))
    }

    @Test
    fun parseTimeInputRejectsInvalidValues() {
        listOf("25", "24:30", "abc", "21:60", "99", "-1", "21:", ":30", "2:3:4").forEach {
            assertEquals(ParsedTime.Invalid, parseTimeInput(it), "'$it' should be invalid")
        }
    }

    @Test
    fun differentFormatsOfSameFilmAreNotGroupedTogether() {
        val groups = listOf(
            showing("Avatar 2D", thursday, "21:00", Cinema.THE_SPACE),
            showing("Avatar 3D", thursday, "19:00", Cinema.UCI),
            showing("Avatar XL", friday, "21:30", Cinema.THE_SPACE)
        ).groupByFilm()

        // Each format variant has a different filmKey, so they are separate groups
        assertEquals(3, groups.size)
        assertEquals(
            listOf("Avatar 2D", "Avatar 3D", "Avatar XL"),
            groups.map { it.title }
        )
    }

    @Test
    fun perDayRangeIsApplied() {
        val showings = listOf(
            showing("Digger", thursday, "19:00", Cinema.THE_SPACE),
            showing("Digger", thursday, "21:30", Cinema.UCI),
            showing("Digger", friday, "21:30", Cinema.UCI),
            showing("Digger", friday, "23:15", Cinema.UCI)
        )
        val ranges = listOf(
            DayRange(thursday, min = LocalTime.parse("21:00"), max = LocalTime.parse("22:30")),
            DayRange(friday, min = LocalTime.parse("22:00"))
        )

        val result = showings.filterShowings(ranges, emptyList(), farPast)

        assertEquals(
            listOf(
                showing("Digger", thursday, "21:30", Cinema.UCI),
                showing("Digger", friday, "23:15", Cinema.UCI)
            ),
            result
        )
    }

    @Test
    fun pastShowingsOfTodayAreDropped() {
        val now = LocalDateTime(2026, 10, 1, 20, 0)
        val showings = listOf(
            showing("Digger", thursday, "19:40", Cinema.THE_SPACE),
            showing("Digger", thursday, "20:00", Cinema.THE_SPACE),
            showing("Digger", thursday, "21:00", Cinema.THE_SPACE)
        )

        val result = showings.filterShowings(listOf(DayRange(thursday)), emptyList(), now)

        assertEquals(listOf(LocalTime.parse("21:00")), result.map { it.time })
    }

    @Test
    fun pollLineMatchesPythonFormat() {
        val line = showing("Digger", thursday, "21:00", Cinema.THE_SPACE).toPollLine()

        assertEquals("Digger (Giovedì - 21:00 - Silea)", line)
    }

    @Test
    fun pollLineAppendsFormatWhenPresent() {
        val day = LocalDate(2026, 10, 1)
        val xl = Showing("Avengers: Endgame Extra", day, LocalTime.parse("20:15"), Cinema.UCI, "XL")
        assertEquals("Avengers: Endgame Extra (Giovedì - 20:15 - Marcon - XL)", xl.toPollLine())
        assertEquals("Avengers: Endgame Extra (Giovedì - 20:15 - Marcon)", xl.copy(format = null).toPollLine())
    }

    @Test
    fun sameTimeInDifferentFormatsIsNotMerged() {
        val day = LocalDate(2026, 10, 1)
        val xl = Showing("Avengers", day, LocalTime.parse("20:15"), Cinema.UCI, "XL")
        val twoD = xl.copy(format = "2D")
        assertEquals(2, listOf(xl, twoD).distinct().size)
    }

    @Test
    fun cleanTitleConvertsAllCapsToTitleCaseAndKeepsOthers() {
        assertEquals("Avengers: Endgame Extra", cleanTitle("AVENGERS: ENDGAME EXTRA"))
        assertEquals("Avengers: Endgame Extra", cleanTitle("Avengers:  Endgame Extra "))
        assertEquals("Linkin Park: Unshatter C.A.", cleanTitle("Linkin Park: Unshatter C.A."))
    }
}

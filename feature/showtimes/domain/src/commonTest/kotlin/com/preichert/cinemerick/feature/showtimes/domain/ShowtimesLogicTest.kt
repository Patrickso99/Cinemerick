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
            showing("HEART OF THE BEAST - NEL PROFONDO SELVAGGIO", thursday, "21:40", Cinema.SILEA),
            showing("Heart of the Beast", thursday, "21:40", Cinema.MARCON),
            showing("Naza", friday, "21:40", Cinema.SILEA),
            showing("Naza C.A.", friday, "19:40", Cinema.MARCON)
        ).groupByFilm()

        // Group titles are the shortest originals
        assertEquals(listOf("Heart of the Beast", "Naza"), groups.map { it.title })
        // Original titles are preserved in showings (not transformed)
        assertEquals(
            listOf("HEART OF THE BEAST - NEL PROFONDO SELVAGGIO", "Heart of the Beast"),
            groups.first().showings.map { it.title }
        )
        assertEquals(
            listOf("Naza", "Naza C.A."),
            groups.last().showings.map { it.title }
        )
        // Cinema and time ordering is preserved
        assertEquals(
            listOf(Cinema.MARCON, Cinema.SILEA),
            groups.first().showings.map { it.cinema }
        )
        assertEquals(
            listOf(LocalTime.parse("19:40"), LocalTime.parse("21:40")),
            groups.last().showings.map { it.time }
        )
    }

    @Test
    fun differentFormatsOfSameFilmAreNotGroupedTogether() {
        val groups = listOf(
            showing("Avatar 2D", thursday, "21:00", Cinema.SILEA),
            showing("Avatar 3D", thursday, "19:00", Cinema.MARCON),
            showing("Avatar XL", friday, "21:30", Cinema.SILEA)
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
            showing("Digger", thursday, "19:00", Cinema.SILEA),
            showing("Digger", thursday, "21:30", Cinema.MARCON),
            showing("Digger", friday, "21:30", Cinema.MARCON),
            showing("Digger", friday, "23:15", Cinema.MARCON)
        )
        val ranges = listOf(
            DayRange(thursday, min = LocalTime.parse("21:00"), max = LocalTime.parse("22:30")),
            DayRange(friday, min = LocalTime.parse("22:00"))
        )

        val result = showings.filterShowings(ranges, emptyList(), farPast)

        assertEquals(
            listOf(
                showing("Digger", thursday, "21:30", Cinema.MARCON),
                showing("Digger", friday, "23:15", Cinema.MARCON)
            ),
            result
        )
    }

    @Test
    fun pastShowingsOfTodayAreDropped() {
        val now = LocalDateTime(2026, 10, 1, 20, 0)
        val showings = listOf(
            showing("Digger", thursday, "19:40", Cinema.SILEA),
            showing("Digger", thursday, "20:00", Cinema.SILEA),
            showing("Digger", thursday, "21:00", Cinema.SILEA)
        )

        val result = showings.filterShowings(listOf(DayRange(thursday)), emptyList(), now)

        assertEquals(listOf(LocalTime.parse("21:00")), result.map { it.time })
    }

    @Test
    fun pollLineMatchesPythonFormat() {
        val line = showing("Digger", thursday, "21:00", Cinema.SILEA).toPollLine()

        assertEquals("Digger (Giovedì - 21:00 - Silea)", line)
    }
}

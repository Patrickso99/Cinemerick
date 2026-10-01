package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalendarMonthTest {

    @Test
    fun monthStartingOnThursdayHasThreeLeadingPads() {
        val grid = monthGrid(2026, Month.OCTOBER)

        assertEquals(List(3) { null }, grid.take(3))
        assertEquals(LocalDate(2026, 10, 1), grid[3])
        assertEquals(LocalDate(2026, 10, 31), grid.last())
        assertEquals(3 + 31, grid.size)
    }

    @Test
    fun monthStartingOnMondayHasNoLeadingPads() {
        val grid = monthGrid(2026, Month.JUNE)

        assertEquals(LocalDate(2026, 6, 1), grid.first())
        assertEquals(30, grid.size)
    }

    @Test
    fun monthStartingOnSundayHasSixLeadingPads() {
        val grid = monthGrid(2026, Month.NOVEMBER)

        assertNull(grid[5])
        assertEquals(LocalDate(2026, 11, 1), grid[6])
    }

    @Test
    fun leapYearFebruaryHas29Days() {
        assertEquals(29, monthGrid(2028, Month.FEBRUARY).filterNotNull().size)
        assertEquals(28, monthGrid(2027, Month.FEBRUARY).filterNotNull().size)
    }

    @Test
    fun weekendFromMidweekIsNextSaturdayAndSunday() {
        assertEquals(
            listOf(LocalDate(2026, 10, 3), LocalDate(2026, 10, 4)),
            upcomingWeekend(LocalDate(2026, 10, 1))
        )
    }

    @Test
    fun weekendOnSaturdayIncludesToday() {
        assertEquals(
            listOf(LocalDate(2026, 10, 3), LocalDate(2026, 10, 4)),
            upcomingWeekend(LocalDate(2026, 10, 3))
        )
    }

    @Test
    fun weekendOnSundayIsOnlyToday() {
        assertEquals(listOf(LocalDate(2026, 10, 4)), upcomingWeekend(LocalDate(2026, 10, 4)))
    }

    @Test
    fun weekendCrossesYearBoundary() {
        assertEquals(
            listOf(LocalDate(2027, 1, 2), LocalDate(2027, 1, 3)),
            upcomingWeekend(LocalDate(2026, 12, 30))
        )
    }
}

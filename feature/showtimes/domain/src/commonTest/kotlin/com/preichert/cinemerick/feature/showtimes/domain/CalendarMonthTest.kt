package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

class CalendarMonthTest {

    @Test
    fun monthStartingOnThursdayHasThreeLeadingPads() {
        val grid = monthGrid(2026, Month.OCTOBER)

        assertThat(grid.take(3)).isEqualTo(List(3) { null })
        assertThat(grid[3]).isEqualTo(LocalDate(2026, 10, 1))
        assertThat(grid.last()).isEqualTo(LocalDate(2026, 10, 31))
        assertThat(grid.size).isEqualTo(3 + 31)
    }

    @Test
    fun monthStartingOnMondayHasNoLeadingPads() {
        val grid = monthGrid(2026, Month.JUNE)

        assertThat(grid.first()).isEqualTo(LocalDate(2026, 6, 1))
        assertThat(grid.size).isEqualTo(30)
    }

    @Test
    fun monthStartingOnSundayHasSixLeadingPads() {
        val grid = monthGrid(2026, Month.NOVEMBER)

        assertThat(grid[5]).isNull()
        assertThat(grid[6]).isEqualTo(LocalDate(2026, 11, 1))
    }

    @Test
    fun leapYearFebruaryHas29Days() {
        assertThat(monthGrid(2028, Month.FEBRUARY).filterNotNull().size).isEqualTo(29)
        assertThat(monthGrid(2027, Month.FEBRUARY).filterNotNull().size).isEqualTo(28)
    }

    @Test
    fun weekendFromMidweekIsNextSaturdayAndSunday() {
        assertThat(upcomingWeekend(LocalDate(2026, 10, 1))).isEqualTo(listOf(LocalDate(2026, 10, 3), LocalDate(2026, 10, 4)))
    }

    @Test
    fun weekendOnSaturdayIncludesToday() {
        assertThat(upcomingWeekend(LocalDate(2026, 10, 3))).isEqualTo(listOf(LocalDate(2026, 10, 3), LocalDate(2026, 10, 4)))
    }

    @Test
    fun weekendOnSundayIsOnlyToday() {
        assertThat(upcomingWeekend(LocalDate(2026, 10, 4))).isEqualTo(listOf(LocalDate(2026, 10, 4)))
    }

    @Test
    fun weekendCrossesYearBoundary() {
        assertThat(upcomingWeekend(LocalDate(2026, 12, 30))).isEqualTo(listOf(LocalDate(2027, 1, 2), LocalDate(2027, 1, 3)))
    }
}

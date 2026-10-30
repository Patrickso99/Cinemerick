package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DaysControllerTest {

    private val first = LocalDate(2026, 11, 1)
    private val second = LocalDate(2026, 11, 2)

    private fun controller(state: MutableStateFlow<ShowtimesState>) = DaysController(
        timeWindowController = TimeWindowController(FakeTimeWindowPreferences(min = 20 * 60, max = 23 * 60)),
        today = { first },
    ) { change -> state.update { change(it) } }

    @Test
    fun aToggledDayStartsWithTheDefaultWindowAndIsRemovedByASecondToggle() {
        val state = MutableStateFlow(ShowtimesState())
        val days = controller(state)

        days.toggle(first)
        assertEquals(listOf(first), state.value.days.map { it.date })
        assertEquals("20:00", state.value.days.single().minTime)
        assertEquals("23:00", state.value.days.single().maxTime)

        days.toggle(first)
        assertEquals(emptyList(), state.value.days)
    }

    @Test
    fun aTimeChangeOnlyTouchesItsOwnDay() {
        val state = MutableStateFlow(ShowtimesState())
        val days = controller(state)
        days.select(listOf(first, second))

        days.setMinTime(first, "18:30")

        assertEquals("18:30", state.value.days.first { it.date == first }.minTime)
        assertEquals("20:00", state.value.days.first { it.date == second }.minTime)
    }

    @Test
    fun aDaySelectedAgainKeepsItsTimeRange() {
        val state = MutableStateFlow(ShowtimesState())
        val days = controller(state)
        days.select(listOf(first))
        days.setMaxTime(first, "21:00")

        days.select(listOf(first, second))

        assertEquals("21:00", state.value.days.first { it.date == first }.maxTime)
    }
}

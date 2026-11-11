package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo

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
        assertThat(state.value.days.map { it.date }).isEqualTo(listOf(first))
        assertThat(state.value.days.single().minTime).isEqualTo("20:00")
        assertThat(state.value.days.single().maxTime).isEqualTo("23:00")

        days.toggle(first)
        assertThat(state.value.days).isEmpty()
    }

    @Test
    fun aTimeChangeOnlyTouchesItsOwnDay() {
        val state = MutableStateFlow(ShowtimesState())
        val days = controller(state)
        days.select(listOf(first, second))

        days.setMinTime(first, "18:30")

        assertThat(state.value.days.first { it.date == first }.minTime).isEqualTo("18:30")
        assertThat(state.value.days.first { it.date == second }.minTime).isEqualTo("20:00")
    }

    @Test
    fun aDaySelectedAgainKeepsItsTimeRange() {
        val state = MutableStateFlow(ShowtimesState())
        val days = controller(state)
        days.select(listOf(first))
        days.setMaxTime(first, "21:00")

        days.select(listOf(first, second))

        assertThat(state.value.days.first { it.date == first }.maxTime).isEqualTo("21:00")
    }
}

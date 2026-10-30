package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.presentation.toTimeInput
import kotlinx.datetime.LocalDate

/**
 * The days to search and the time range of each one, plus the calendar that picks them. A new day starts with the default
 * time window from the settings; a day that stays selected keeps its own range.
 */
internal class DaysController(
    private val timeWindowController: TimeWindowController,
    private val today: () -> LocalDate,
    private val update: StateUpdate,
) {
    fun openCalendar() {
        update { it.copy(calendar = CalendarUi(minDate = today())) }
    }

    fun dismissCalendar() {
        update { it.copy(calendar = null) }
    }

    // Keeps the time ranges of dates that stay selected.
    fun select(dates: List<LocalDate>) {
        update { state ->
            state.copy(days = dates.map { date -> state.days.firstOrNull { it.date == date } ?: newDay(date) })
        }
    }

    fun confirm(dates: Set<LocalDate>) {
        select(dates.sorted())
        update { it.copy(calendar = null) }
    }

    fun toggle(date: LocalDate) {
        update { state ->
            val days = if (state.days.any { it.date == date }) {
                state.days.filterNot { it.date == date }
            } else {
                (state.days + newDay(date)).sortedBy { it.date }
            }
            state.copy(days = days)
        }
    }

    fun setMinTime(date: LocalDate, value: String) {
        updateDay(date) { it.copy(minTime = value) }
    }

    fun setMaxTime(date: LocalDate, value: String) {
        updateDay(date) { it.copy(maxTime = value) }
    }

    private fun newDay(date: LocalDate): DayUi {
        val window = timeWindowController.window.value
        return DayUi(
            date = date,
            isSelected = true,
            minTime = window.minMinutes.toTimeInput(),
            maxTime = window.maxMinutes.toTimeInput()
        )
    }

    private fun updateDay(date: LocalDate, transform: (DayUi) -> DayUi) {
        update { state ->
            state.copy(days = state.days.map { if (it.date == date) transform(it) else it })
        }
    }
}

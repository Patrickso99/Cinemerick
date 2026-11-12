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
    // The dialog starts from the days already chosen, on the current month.
    fun openCalendar() {
        val minDate = today()
        update { state ->
            state.copy(calendar = CalendarUi(minDate = minDate, draft = state.days.mapTo(mutableSetOf()) { it.date }))
        }
    }

    // A day before the first selectable one is ignored: the calendar shows it disabled.
    fun toggleDraft(date: LocalDate) {
        updateCalendar { calendar ->
            when {
                date in calendar.draft -> calendar.copy(draft = calendar.draft - date)
                date < calendar.minDate -> calendar
                else -> calendar.copy(draft = calendar.draft + date)
            }
        }
    }

    fun clearDraft() {
        updateCalendar { it.copy(draft = emptySet()) }
    }

    // The month on screen: of the dialog while it is open, otherwise of the inline calendar. Never before the current month.
    fun showMonth(month: LocalDate) {
        update { state ->
            val calendar = state.calendar
            if (calendar != null) {
                state.copy(calendar = calendar.copy(shownMonth = maxOf(month.firstOfMonth(), calendar.minDate.firstOfMonth())))
            } else {
                state.copy(inlineCalendarMonth = maxOf(month.firstOfMonth(), today().firstOfMonth()))
            }
        }
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

    // Replaces the days with the ones picked in the dialog. Returns false when the dialog is not open.
    fun confirm(): Boolean {
        var confirmed = false
        update { state ->
            val calendar = state.calendar ?: return@update state
            confirmed = true
            state.copy(
                days = calendar.sortedDraft.map { date -> state.days.firstOrNull { it.date == date } ?: newDay(date) },
                calendar = null
            )
        }
        return confirmed
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

    fun newDay(date: LocalDate): DayUi {
        val window = timeWindowController.window.value
        return DayUi(
            date = date,
            isSelected = true,
            minTime = window.minMinutes.toTimeInput(),
            maxTime = window.maxMinutes.toTimeInput()
        )
    }

    private fun updateCalendar(transform: (CalendarUi) -> CalendarUi) {
        update { state -> state.copy(calendar = state.calendar?.let(transform)) }
    }

    private fun updateDay(date: LocalDate, transform: (DayUi) -> DayUi) {
        update { state ->
            state.copy(days = state.days.map { if (it.date == date) transform(it) else it })
        }
    }
}

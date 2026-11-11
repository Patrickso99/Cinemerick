package com.preichert.cinemerick.feature.showtimes.presentation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlin.test.Test

/** The days picked in the calendar dialog are a draft: they replace the selected days only on Done. */
class CalendarDraftTest {

    private val today = LocalDate(2026, 11, 30)
    private val yesterday = LocalDate(2026, 11, 29)
    private val tomorrow = LocalDate(2026, 12, 1)
    private val nextWeek = LocalDate(2026, 12, 7)

    private val state = MutableStateFlow(ShowtimesState())
    private val days = DaysController(
        timeWindowController = TimeWindowController(FakeTimeWindowPreferences(min = 20 * 60, max = null)),
        today = { today },
    ) { change -> state.update { change(it) } }

    private val calendar get() = state.value.calendar

    @Test
    fun theDialogStartsFromTheChosenDaysOnTheMonthOfToday() {
        days.select(listOf(tomorrow, nextWeek))

        days.openCalendar()

        assertThat(calendar?.draft).isEqualTo(setOf(tomorrow, nextWeek))
        assertThat(calendar?.minDate).isEqualTo(today)
        assertThat(calendar?.shownMonth).isEqualTo(LocalDate(2026, 11, 1))
    }

    @Test
    fun pickingInTheDialogLeavesTheDaysAloneUntilDone() {
        days.select(listOf(tomorrow))
        days.openCalendar()

        days.toggleDraft(nextWeek)
        days.toggleDraft(tomorrow)

        assertThat(state.value.days.map { it.date }).containsExactly(tomorrow)
        assertThat(calendar?.draft).isEqualTo(setOf(nextWeek))
    }

    @Test
    fun dismissingTheDialogDropsTheDraft() {
        days.select(listOf(tomorrow))
        days.openCalendar()
        days.toggleDraft(nextWeek)

        days.dismissCalendar()

        assertThat(calendar).isNull()
        assertThat(state.value.days.map { it.date }).containsExactly(tomorrow)
    }

    @Test
    fun doneReplacesTheDaysWithTheDraftInDateOrderKeepingTheirRanges() {
        days.select(listOf(tomorrow))
        days.setMinTime(tomorrow, "15:00")
        days.openCalendar()
        days.toggleDraft(nextWeek)
        days.toggleDraft(today)

        assertThat(days.confirm()).isTrue()

        assertThat(state.value.days.map { it.date }).containsExactly(today, tomorrow, nextWeek)
        assertThat(state.value.days.first { it.date == tomorrow }.minTime).isEqualTo("15:00")
        assertThat(state.value.days.first { it.date == today }.minTime).isEqualTo("20:00")
        assertThat(calendar).isNull()
    }

    @Test
    fun aSecondDoneDoesNothing() {
        days.openCalendar()
        days.toggleDraft(tomorrow)
        days.confirm()
        days.toggle(nextWeek)

        assertThat(days.confirm()).isFalse()

        assertThat(state.value.days.map { it.date }).containsExactly(tomorrow, nextWeek)
    }

    @Test
    fun aDayBeforeTodayCanLeaveTheDraftButNotEnterIt() {
        // A day chosen yesterday is still selected when the dialog opens today.
        days.select(listOf(yesterday))
        days.openCalendar()

        days.toggleDraft(yesterday)
        assertThat(calendar?.draft).isNotNull().isEmpty()

        days.toggleDraft(yesterday)
        assertThat(calendar?.draft).isNotNull().isEmpty()
    }

    @Test
    fun clearEmptiesOnlyTheDraft() {
        days.select(listOf(tomorrow))
        days.openCalendar()

        days.clearDraft()

        assertThat(calendar?.draft).isNotNull().isEmpty()
        assertThat(state.value.days.map { it.date }).containsExactly(tomorrow)
    }

    @Test
    fun theDialogCannotGoToAMonthBeforeToday() {
        days.openCalendar()

        days.showMonth(LocalDate(2026, 10, 1))
        assertThat(calendar?.shownMonth).isEqualTo(LocalDate(2026, 11, 1))

        // A day inside a month stands for the whole month.
        days.showMonth(LocalDate(2027, 1, 15))
        assertThat(calendar?.shownMonth).isEqualTo(LocalDate(2027, 1, 1))
    }

    @Test
    fun withoutTheDialogTheMonthIsTheInlineCalendars() {
        days.showMonth(LocalDate(2026, 12, 3))
        assertThat(state.value.inlineCalendarMonth).isEqualTo(LocalDate(2026, 12, 1))

        days.showMonth(LocalDate(2026, 9, 1))
        assertThat(state.value.inlineCalendarMonth).isEqualTo(LocalDate(2026, 11, 1))
    }

    @Test
    fun theDraftCannotChangeWhileTheDialogIsClosed() {
        days.toggleDraft(tomorrow)
        days.clearDraft()

        assertThat(calendar).isNull()
        assertThat(state.value.days).isEmpty()
    }
}

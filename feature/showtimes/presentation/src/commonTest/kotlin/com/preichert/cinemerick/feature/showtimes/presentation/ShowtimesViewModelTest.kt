package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.data.SettingsTimeWindowPreferences
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.MemorySettings
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShowtimesViewModelTest {

    private val first = LocalDate(2026, 10, 1)
    private val second = LocalDate(2026, 10, 2)

    private val controller = TimeWindowController(SettingsTimeWindowPreferences(MemorySettings()))

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ShowtimesViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        return ShowtimesViewModel(FakeRepository, FakeFilterPreferences, controller)
    }

    private fun ShowtimesViewModel.day(date: LocalDate) = state.value.days.first { it.date == date }

    @Test
    fun newDayUsesConfiguredWindow() = runTest {
        controller.setMin(18 * 60)
        controller.setMax(23 * 60)
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertEquals("18:00", viewModel.day(first).minTime)
        assertEquals("23:00", viewModel.day(first).maxTime)
    }

    @Test
    fun unsetMaxGivesBlankMax() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertEquals("20:00", viewModel.day(first).minTime)
        assertEquals("", viewModel.day(first).maxTime)
    }

    @Test
    fun changingWindowOnlyAffectsDaysSelectedAfterwards() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        controller.setMin(18 * 60)
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        assertEquals("20:00", viewModel.day(first).minTime)
        assertEquals("18:00", viewModel.day(second).minTime)
    }

    @Test
    fun confirmKeepsTimeRangeOfRetainedDays() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnMinTimeChange(first, "15:00"))

        controller.setMin(18 * 60)
        viewModel.onAction(ShowtimesAction.OnDatesConfirm(setOf(second, first)))

        assertEquals("15:00", viewModel.day(first).minTime)
        assertEquals("18:00", viewModel.day(second).minTime)
    }

    @Test
    fun confirmDropsRemovedDaysAndSortsByDate() = runTest {
        val third = LocalDate(2026, 10, 3)
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        viewModel.onAction(ShowtimesAction.OnDatesConfirm(setOf(third, second)))

        assertEquals(listOf(second, third), viewModel.state.value.days.map { it.date })
    }

    @Test
    fun confirmClosesCalendar() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)

        viewModel.onAction(ShowtimesAction.OnDatesConfirm(setOf(first)))

        assertNull(viewModel.state.value.calendar)
    }

    @Test
    fun dismissLeavesDaysUnchanged() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)

        viewModel.onAction(ShowtimesAction.OnCalendarDismiss)

        assertEquals(listOf(first), viewModel.state.value.days.map { it.date })
        assertNull(viewModel.state.value.calendar)
    }

    @Test
    fun chipToggleStillAppliesImmediately() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertEquals(listOf(second), viewModel.state.value.days.map { it.date })
    }

    private object FakeRepository : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = emptyMap()
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
    }

    private object FakeFilterPreferences : FilterPreferences {
        override fun getSelectedVenues(): Set<Venue>? = null
        override fun setSelectedVenues(venues: Set<Venue>) = Unit
    }
}

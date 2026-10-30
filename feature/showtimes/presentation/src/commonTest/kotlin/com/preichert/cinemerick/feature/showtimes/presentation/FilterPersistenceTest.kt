package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterPersistenceTest {

    private val day = LocalDate(2026, 11, 1)

    @Test
    fun daysTimesFilmFilterAndTagsSurviveARestore() = runTest {
        val savedStateHandle = SavedStateHandle()
        val state = MutableStateFlow(
            ShowtimesState(
                days = listOf(DayUi(day, isSelected = true, minTime = "18:30", maxTime = "23:00")),
                filmFilter = "Dune",
                hiddenTags = setOf("3D"),
            )
        )
        // Unconfined, as the view model's main dispatcher is in the other tests: the save runs as the state is set.
        val saving = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
        FilterPersistence(savedStateHandle).track(saving, state)

        val restored = FilterPersistence(savedStateHandle).restore(ShowtimesState())

        assertEquals(listOf(DayUi(day, isSelected = true, minTime = "18:30", maxTime = "23:00")), restored.days)
        assertEquals("Dune", restored.filmFilter)
        assertEquals(setOf("3D"), restored.hiddenTags)
    }

    @Test
    fun anUnknownDateIsSkippedInsteadOfFailingTheRestore() {
        val savedStateHandle = SavedStateHandle(
            mapOf("filters" to """{"days":[{"date":"not-a-date","isSelected":true,"minTime":"20:00","maxTime":"23:00"}]}""")
        )

        assertEquals(emptyList(), FilterPersistence(savedStateHandle).restore(ShowtimesState()).days)
    }
}

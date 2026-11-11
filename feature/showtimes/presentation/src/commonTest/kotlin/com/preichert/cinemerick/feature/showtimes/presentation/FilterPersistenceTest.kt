package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo

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

        assertThat(restored.days).isEqualTo(listOf(DayUi(day, isSelected = true, minTime = "18:30", maxTime = "23:00")))
        assertThat(restored.filmFilter).isEqualTo("Dune")
        assertThat(restored.hiddenTags).isEqualTo(setOf("3D"))
    }

    @Test
    fun anUnknownDateIsSkippedInsteadOfFailingTheRestore() {
        val savedStateHandle = SavedStateHandle(
            mapOf("filters" to """{"days":[{"date":"not-a-date","isSelected":true,"minTime":"20:00","maxTime":"23:00"}]}""")
        )

        assertThat(FilterPersistence(savedStateHandle).restore(ShowtimesState()).days).isEmpty()
    }
}

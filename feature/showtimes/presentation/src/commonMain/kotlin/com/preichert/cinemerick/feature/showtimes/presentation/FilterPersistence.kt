package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The filters that survive process death: the days and their time ranges, the film filter and the hidden format tags.
 * They are kept in the saved state as JSON, so restoring them does not depend on the platform.
 */
internal class FilterPersistence(private val savedStateHandle: SavedStateHandle) {

    // A saved state that cannot be read is ignored: the filters then start from their defaults.
    fun restore(state: ShowtimesState): ShowtimesState {
        val saved = runCatching {
            savedStateHandle.get<String>(KEY)?.let { Json.decodeFromString(SavedFilters.serializer(), it) }
        }.getOrNull() ?: SavedFilters()
        return state.copy(
            days = saved.days.mapNotNull { day ->
                runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { date ->
                    DayUi(date, day.isSelected, day.minTime, day.maxTime)
                }
            },
            filmFilter = saved.filmFilter,
            hiddenTags = saved.hiddenTags.toSet(),
        )
    }

    // Every filter change is saved from now on, so a restored process starts from the same days, times and tags.
    fun track(scope: CoroutineScope, states: Flow<ShowtimesState>) {
        scope.launch {
            states.map { it.toSavedFilters() }.distinctUntilChanged().collect { saved ->
                savedStateHandle[KEY] = Json.encodeToString(SavedFilters.serializer(), saved)
            }
        }
    }

    private fun ShowtimesState.toSavedFilters() = SavedFilters(
        days = days.map { SavedDay(it.date.toString(), it.isSelected, it.minTime, it.maxTime) },
        filmFilter = filmFilter,
        hiddenTags = hiddenTags.sorted(),
    )

    private companion object {
        const val KEY = "filters"
    }
}

@Serializable
private data class SavedFilters(
    val days: List<SavedDay> = emptyList(),
    val filmFilter: String = "",
    val hiddenTags: List<String> = emptyList(),
)

@Serializable
private data class SavedDay(val date: String, val isSelected: Boolean, val minTime: String, val maxTime: String)

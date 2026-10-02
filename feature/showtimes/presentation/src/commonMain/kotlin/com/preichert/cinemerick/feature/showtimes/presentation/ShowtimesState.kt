package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.ParsedTime
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.parseTimeInput
import kotlinx.datetime.LocalDate

@Stable
data class ShowtimesState(
    val days: List<DayUi> = emptyList(),
    val calendar: CalendarUi? = null,
    val filmFilter: String = "",
    val selectedVenues: Set<Venue> = emptySet(),
    val isLoading: Boolean = false,
    val hasGenerated: Boolean = false,
    val groups: List<FilmGroupUi> = emptyList(),
    val pollText: String = "",
    val optionsCount: Int = 0,
    val venueErrors: List<VenueErrorUi> = emptyList(),
    val hiddenTags: Set<String> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val elapsedTimeMillis: Long? = null,
    val venueCatalog: Map<Chain, VenueCatalogUi> = emptyMap(),
    val venuePicker: VenuePickerUi? = null
) {
    val canClear: Boolean get() = !isLoading && (days.isNotEmpty() || filmFilter.isNotEmpty() || hasGenerated || selectedVenues.isNotEmpty())
}

/** Calendar dialog state: present while the dialog is open. */
data class CalendarUi(val minDate: LocalDate)

data class DayUi(
    val date: LocalDate,
    val label: String,
    val isSelected: Boolean = false,
    val minTime: String = DEFAULT_MIN_TIME,
    val maxTime: String = ""
) {
    val isMinTimeError: Boolean get() = parseTimeInput(minTime) is ParsedTime.Invalid
    val isMaxTimeError: Boolean get() = parseTimeInput(maxTime) is ParsedTime.Invalid

    companion object {
        const val DEFAULT_MIN_TIME = "20:00"
    }
}

data class FilmGroupUi(
    val title: String,
    val showings: List<ShowingUi>,
    val posterUrl: String? = null
)

data class ShowingUi(
    val day: String,
    val date: String,
    val time: String,
    val venue: Venue,
    val format: String? = null
)

data class VenueErrorUi(
    val venueName: String,
    val message: UiText
)

sealed interface VenueCatalogUi {
    data object Loading : VenueCatalogUi
    data class Loaded(val venues: List<Venue>) : VenueCatalogUi
    data class Error(val message: UiText) : VenueCatalogUi
}

data class VenuePickerUi(
    val chain: Chain,
    val query: String = "",
    val catalog: VenueCatalogUi = VenueCatalogUi.Loading
)

fun filteredVenues(venues: List<Venue>, query: String): List<Venue> {
    if (query.isBlank()) return venues
    return venues.filter { venue ->
        venue.name.contains(query, ignoreCase = true) ||
            (venue.region?.contains(query, ignoreCase = true) ?: false)
    }
}

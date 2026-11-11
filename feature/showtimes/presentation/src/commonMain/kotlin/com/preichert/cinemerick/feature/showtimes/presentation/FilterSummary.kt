package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.Immutable
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import kotlinx.datetime.LocalDate

/** A filter component the summary bar can jump to. */
sealed interface FilterTarget {
    /** The card of one chain, opened on arrival. */
    data class ChainCard(val chain: Chain) : FilterTarget

    /** The Cinemas section, when no venue is selected. */
    data object Cinemas : FilterTarget

    /** The Days section, when no day is selected. */
    data object Days : FilterTarget

    /** The time window slider of one selected day. */
    data class TimeWindow(val date: LocalDate) : FilterTarget

    data object Formats : FilterTarget

    data object FilmFilter : FilterTarget
}

/** What the filter summary bar shows: the current filters, without any text formatting. */
@Immutable
data class FilterSummaryUi(
    /** Chains with at least one selected venue, in the order of [Chain]. */
    val chains: List<ChainSummaryUi> = emptyList(),
    val venueCount: Int = 0,
    /** Selected days in date order, each with its time window. */
    val days: List<DaySummaryUi> = emptyList(),
    /** Format tags the user hid, only once there are results to hide them from. */
    val hiddenFormats: List<String> = emptyList(),
    val filmQueries: List<String> = emptyList(),
    /** The film filter differs from the one the results on screen were searched with. */
    val isFilmFilterChanged: Boolean = false,
    val isSearching: Boolean = false,
) {
    /** The results are out of date: the bar asks to update them. */
    val isStale: Boolean get() = isFilmFilterChanged && !isSearching

    /** The window shared by every selected day, or null when the days have different windows (or there are none). */
    val sharedWindow: Pair<Int, Int>? get() = days.map { it.startMinutes to it.endMinutes }.distinct().singleOrNull()
}

@Immutable
data class ChainSummaryUi(val chain: Chain, val venues: List<String>)

/** A selected day and its window in minutes from midnight; 1440 is the end of the day. */
@Immutable
data class DaySummaryUi(val date: LocalDate, val startMinutes: Int, val endMinutes: Int)

internal fun ShowtimesState.toFilterSummary(): FilterSummaryUi = FilterSummaryUi(
    chains = Chain.entries.mapNotNull { chain ->
        selectedVenues.filter { it.chain == chain }
            .takeIf { it.isNotEmpty() }
            ?.let { venues -> ChainSummaryUi(chain, venues.map { it.name }.sorted()) }
    },
    venueCount = selectedVenues.size,
    days = days.filter { it.isSelected }.sortedBy { it.date }.map { day ->
        val (start, end) = day.windowMinutes()
        DaySummaryUi(day.date, start, end)
    },
    hiddenFormats = if (hasGenerated && availableTags.isNotEmpty()) hiddenTags.sorted() else emptyList(),
    filmQueries = filmQueriesOf(filmFilter),
    isFilmFilterChanged = hasGenerated && searchedFilmFilter != null && filmQueriesOf(filmFilter) != filmQueriesOf(searchedFilmFilter),
    isSearching = isLoading,
)

/** The film titles of a comma-separated filter, as the search reads them: trimmed, blanks dropped. */
internal fun filmQueriesOf(filter: String): List<String> = filter.split(",").map { it.trim() }.filter { it.isNotEmpty() }

package com.preichert.cinemerick.feature.showtimes.presentation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class FilterSummaryTest {

    private val thu = LocalDate(2026, 11, 13)
    private val fri = LocalDate(2026, 11, 14)

    private fun venue(chain: Chain, name: String) = Venue(chain, name.lowercase(), name, "Veneto")

    @Test
    fun venuesAreGroupedByChainInChainOrderWithSortedNames() {
        val state = ShowtimesState(
            selectedVenues = setOf(
                venue(Chain.NOTORIOUS, "Fiume Veneto"),
                venue(Chain.UCI, "UCI Villesse"),
                venue(Chain.UCI, "UCI Luxe Marcon"),
                venue(Chain.THE_SPACE, "Silea"),
            )
        )

        val summary = state.toFilterSummary()

        assertThat(summary.chains).containsExactly(
            ChainSummaryUi(Chain.THE_SPACE, listOf("Silea")),
            ChainSummaryUi(Chain.UCI, listOf("UCI Luxe Marcon", "UCI Villesse")),
            ChainSummaryUi(Chain.NOTORIOUS, listOf("Fiume Veneto")),
        )
        assertThat(summary.venueCount).isEqualTo(4)
    }

    @Test
    fun onlySelectedDaysAreListedInDateOrderWithTheirWindow() {
        val state = ShowtimesState(
            days = listOf(
                DayUi(fri, isSelected = true, minTime = "18:00", maxTime = "23:59"),
                DayUi(LocalDate(2026, 11, 15), isSelected = false, minTime = "", maxTime = ""),
                DayUi(thu, isSelected = true, minTime = "", maxTime = ""),
            )
        )

        val days = state.toFilterSummary().days

        // A blank bound is the edge of the day; 23:59 is read back as the end of the day, like the slider does.
        assertThat(days).containsExactly(DaySummaryUi(thu, 0, 1440), DaySummaryUi(fri, 18 * 60, 1440))
    }

    @Test
    fun theSharedWindowIsNullWhenTheDaysDiffer() {
        val same = FilterSummaryUi(days = listOf(DaySummaryUi(thu, 1200, 1440), DaySummaryUi(fri, 1200, 1440)))
        val different = same.copy(days = listOf(DaySummaryUi(thu, 1200, 1440), DaySummaryUi(fri, 1080, 1440)))

        assertThat(same.sharedWindow).isEqualTo(1200 to 1440)
        assertThat(different.sharedWindow).isNull()
        assertThat(FilterSummaryUi().sharedWindow).isNull()
    }

    @Test
    fun hiddenFormatsAppearOnlyOnceThereAreResultsToHideThemFrom() {
        val restored = ShowtimesState(hiddenTags = setOf("3D", "4DX"))
        val searched = restored.copy(hasGenerated = true, availableTags = listOf("2D", "3D", "4DX"))

        assertThat(restored.toFilterSummary().hiddenFormats).isEmpty()
        assertThat(searched.toFilterSummary().hiddenFormats).containsExactly("3D", "4DX")
    }

    @Test
    fun theFilmFilterIsChangedOnlyWhenItsTitlesDifferFromTheSearchedOnes() {
        val searched = ShowtimesState(hasGenerated = true, filmFilter = "Dune, Wicked", searchedFilmFilter = "Dune, Wicked")

        assertThat(searched.toFilterSummary().isFilmFilterChanged).isFalse()
        // Spaces and empty entries do not change what is searched.
        assertThat(searched.copy(filmFilter = " Dune ,Wicked, ").toFilterSummary().isFilmFilterChanged).isFalse()
        assertThat(searched.copy(filmFilter = "Dune").toFilterSummary().isFilmFilterChanged).isTrue()
    }

    @Test
    fun nothingIsChangedBeforeTheFirstSearch() {
        val state = ShowtimesState(filmFilter = "Dune")

        assertThat(state.toFilterSummary().isFilmFilterChanged).isFalse()
    }

    @Test
    fun aRunningSearchIsNotStale() {
        val changed = FilterSummaryUi(isFilmFilterChanged = true)

        assertThat(changed.isStale).isTrue()
        assertThat(changed.copy(isSearching = true).isStale).isFalse()
    }

    @Test
    fun filmQueriesKeepUnicodeAndDropBlanks() {
        assertThat(filmQueriesOf("Amélie, ,🎬 Zootropolis 2,,")).containsExactly("Amélie", "🎬 Zootropolis 2")
        assertThat(filmQueriesOf("   ")).isEmpty()
    }
}

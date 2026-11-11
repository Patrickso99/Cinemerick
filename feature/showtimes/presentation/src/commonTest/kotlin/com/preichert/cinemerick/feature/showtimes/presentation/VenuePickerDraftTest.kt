package com.preichert.cinemerick.feature.showtimes.presentation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

/** The venues ticked in the picker are a draft: they replace the selected venues only on Done. */
class VenuePickerDraftTest {

    private val marcon = Venue(Chain.UCI, "uci-marcon", "UCI Luxe Marcon", "Veneto")
    private val mestre = Venue(Chain.UCI, "uci-mestre", "UCI Mestre", "Veneto")
    private val silea = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")

    private val state = MutableStateFlow(ShowtimesState())
    private var venueAnswers = mutableListOf<Result<List<Venue>, DataError.Network>>()

    private val repository = object : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> =
            mapOf(Chain.UCI to (venueAnswers.removeFirstOrNull() ?: Result.Success(listOf(marcon, mestre))))
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private val preferences = object : FilterPreferences {
        var saves = 0
        override fun getSelectedVenues(): Set<Venue>? = null
        override fun setSelectedVenues(venues: Set<Venue>) {
            saves++
        }
    }

    private fun TestScope.picker() =
        VenuePickerController(this, repository, preferences) { change -> state.update { change(it) } }

    private val draft get() = state.value.venuePicker?.draft

    @Test
    fun thePickerStartsFromTheSelectedVenues() = runTest {
        state.update { it.copy(selectedVenues = setOf(silea)) }

        picker().open(Chain.UCI)

        assertThat(draft).isEqualTo(setOf(silea))
    }

    @Test
    fun tickingLeavesTheSelectionAndThePreferencesAloneUntilDone() = runTest {
        val picker = picker()
        picker.open(Chain.UCI)
        advanceUntilIdle()

        picker.toggleDraft(marcon)

        assertThat(state.value.selectedVenues).isEqualTo(emptySet())
        assertThat(preferences.saves).isEqualTo(0)
        assertThat(state.value.venuePicker?.selectedCount).isEqualTo(1)
    }

    @Test
    fun doneMakesTheDraftTheSelectionAndClosesThePicker() = runTest {
        state.update { it.copy(selectedVenues = setOf(silea)) }
        val picker = picker()
        picker.open(Chain.UCI)
        picker.toggleDraft(marcon)

        assertThat(picker.confirm()).isTrue()

        assertThat(state.value.selectedVenues).isEqualTo(setOf(silea, marcon))
        assertThat(state.value.venuePicker).isNull()
    }

    @Test
    fun doneWithoutAChangeReportsNoChange() = runTest {
        state.update { it.copy(selectedVenues = setOf(silea)) }
        val picker = picker()
        picker.open(Chain.UCI)
        picker.toggleDraft(marcon)
        picker.toggleDraft(marcon)

        assertThat(picker.confirm()).isFalse()
        // A second Done, with the picker already closed, changes nothing either.
        assertThat(picker.confirm()).isFalse()
        assertThat(state.value.selectedVenues).isEqualTo(setOf(silea))
    }

    @Test
    fun closingThePickerDropsTheDraft() = runTest {
        val picker = picker()
        picker.open(Chain.UCI)
        picker.toggleDraft(marcon)

        picker.dismiss()

        assertThat(state.value.selectedVenues).isEqualTo(emptySet())
        assertThat(state.value.venuePicker).isNull()
    }

    @Test
    fun selectAllOnlyTicksTheVenuesTheSearchLeaves() = runTest {
        val picker = picker()
        picker.open(Chain.UCI)
        advanceUntilIdle()
        picker.search("marcon")

        picker.selectAllVisible()

        assertThat(draft).isEqualTo(setOf(marcon))
    }

    @Test
    fun deselectAllKeepsTheVenuesOfOtherChainsAndHiddenOnes() = runTest {
        state.update { it.copy(selectedVenues = setOf(silea, marcon, mestre)) }
        val picker = picker()
        picker.open(Chain.UCI)
        advanceUntilIdle()
        picker.search("mestre")

        picker.deselectAllVisible()

        assertThat(draft).isEqualTo(setOf(silea, marcon))
    }

    @Test
    fun theCountIsOnlyOfTheChainOnScreen() = runTest {
        state.update { it.copy(selectedVenues = setOf(silea, marcon)) }

        picker().open(Chain.UCI)

        assertThat(state.value.venuePicker?.selectedCount).isEqualTo(1)
    }

    @Test
    fun reloadingAfterAFailureKeepsTheSearchAndTheTicks() = runTest {
        venueAnswers += Result.Failure(DataError.Network.NO_INTERNET)
        val picker = picker()
        picker.open(Chain.UCI)
        advanceUntilIdle()
        assertThat(state.value.venuePicker?.catalog).isNotNull().isInstanceOf<VenueCatalogUi.Error>()
        picker.search("mar")
        picker.toggleDraft(marcon)

        picker.reload()
        assertThat(state.value.venuePicker?.catalog).isEqualTo(VenueCatalogUi.Loading)
        advanceUntilIdle()

        val reloaded = state.value.venuePicker
        assertThat(reloaded?.catalog).isEqualTo(VenueCatalogUi.Loaded(listOf(marcon, mestre)))
        assertThat(reloaded?.query).isEqualTo("mar")
        assertThat(reloaded?.draft).isEqualTo(setOf(marcon))
        assertThat(reloaded?.visibleVenues.orEmpty()).containsExactly(marcon)
    }

    @Test
    fun aCatalogThatArrivesForAnotherChainIsNotShown() = runTest {
        val picker = picker()
        picker.open(Chain.UCI)
        // The user switches chain before the UCI venues arrive.
        picker.open(Chain.THE_SPACE)
        advanceUntilIdle()

        val shown = state.value.venuePicker
        assertThat(shown?.chain).isEqualTo(Chain.THE_SPACE)
        assertThat(shown?.catalog).isNotNull().isInstanceOf<VenueCatalogUi.Error>()
    }
}

package com.preichert.cinemerick.feature.showtimes.presentation

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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VenuePickerControllerTest {

    private val uci = Venue(Chain.UCI, "uci-marcon", "UCI Luxe Marcon", "Veneto")
    private val space = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")

    private val repository = object : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> =
            mapOf(Chain.UCI to Result.Success(listOf(uci)))
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private class MemoryPreferences : FilterPreferences {
        var saved: Set<Venue>? = null
        override fun getSelectedVenues(): Set<Venue>? = saved
        override fun setSelectedVenues(venues: Set<Venue>) {
            saved = venues
        }
    }

    @Test
    fun togglingAVenueSelectsItAndSavesTheSelection() = runTest {
        val state = MutableStateFlow(ShowtimesState())
        val preferences = MemoryPreferences()
        val picker = VenuePickerController(this, repository, preferences) { change -> state.update { change(it) } }

        picker.toggle(space)
        assertEquals(setOf(space), state.value.selectedVenues)
        assertEquals(setOf(space), preferences.saved)

        picker.toggle(space)
        assertEquals(emptySet(), state.value.selectedVenues)
        assertEquals(emptySet(), preferences.saved)
    }

    @Test
    fun openingAChainLoadsItsCatalogAndSearchSetsTheQuery() = runTest {
        val state = MutableStateFlow(ShowtimesState())
        val picker = VenuePickerController(this, repository, MemoryPreferences()) { change -> state.update { change(it) } }

        picker.open(Chain.UCI)
        advanceUntilIdle()
        picker.search("Marcon")

        val venuePicker = state.value.venuePicker
        assertEquals(Chain.UCI, venuePicker?.chain)
        assertEquals("Marcon", venuePicker?.query)
        assertEquals(VenueCatalogUi.Loaded(listOf(uci)), venuePicker?.catalog)
    }

    @Test
    fun dismissingTheSheetClosesIt() = runTest {
        val state = MutableStateFlow(ShowtimesState())
        val picker = VenuePickerController(this, repository, MemoryPreferences()) { change -> state.update { change(it) } }
        picker.open(Chain.UCI)
        advanceUntilIdle()

        picker.dismiss()

        assertNull(state.value.venuePicker)
    }
}

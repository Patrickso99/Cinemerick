package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.toUiText
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.venues_load_error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The venue picker: which cinemas of a chain are on offer, the search in them, and the selected venues. The selection is
 * kept in the filter preferences, so it survives a restart. Only used on the main thread, like the view model.
 */
internal class VenuePickerController(
    private val scope: CoroutineScope,
    private val showtimesRepository: ShowtimesRepository,
    private val filterPreferences: FilterPreferences,
    private val update: StateUpdate,
) {
    fun open(chain: Chain) {
        update { it.copy(venuePicker = VenuePickerUi(chain = chain)) }
        scope.launch {
            val catalog = showtimesRepository.getVenues()
            val chainVenues = catalog[chain]
            val catalogUi = when (chainVenues) {
                is Result.Success -> VenueCatalogUi.Loaded(chainVenues.data)
                is Result.Failure -> VenueCatalogUi.Error(chainVenues.error.toUiText())
                null -> VenueCatalogUi.Error(UiText.Resource(Res.string.venues_load_error))
            }
            update { state ->
                state.copy(venuePicker = state.venuePicker?.copy(catalog = catalogUi))
            }
        }
    }

    fun search(query: String) {
        update { state -> state.copy(venuePicker = state.venuePicker?.copy(query = query)) }
    }

    fun dismiss() {
        update { it.copy(venuePicker = null) }
    }

    fun toggle(venue: Venue) {
        update { state ->
            val venues = if (venue in state.selectedVenues) state.selectedVenues - venue else state.selectedVenues + venue
            filterPreferences.setSelectedVenues(venues)
            state.copy(selectedVenues = venues)
        }
    }
}

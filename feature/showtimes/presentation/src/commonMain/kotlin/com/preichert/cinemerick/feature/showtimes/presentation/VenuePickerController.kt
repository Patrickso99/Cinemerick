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
    // The picker starts from the venues already selected.
    fun open(chain: Chain) {
        update { it.copy(venuePicker = VenuePickerUi(chain = chain, draft = it.selectedVenues)) }
        loadCatalog(chain)
    }

    // Asks for the venues again after a failure, keeping the search and what was ticked so far.
    fun reload() {
        var chain: Chain? = null
        update { state ->
            val picker = state.venuePicker ?: return@update state
            chain = picker.chain
            state.copy(venuePicker = picker.copy(catalog = VenueCatalogUi.Loading))
        }
        chain?.let(::loadCatalog)
    }

    private fun loadCatalog(chain: Chain) {
        scope.launch {
            val catalog = showtimesRepository.getVenues()
            val chainVenues = catalog[chain]
            val catalogUi = when (chainVenues) {
                is Result.Success -> VenueCatalogUi.Loaded(chainVenues.data)
                is Result.Failure -> VenueCatalogUi.Error(chainVenues.error.toUiText())
                null -> VenueCatalogUi.Error(UiText.Resource(Res.string.venues_load_error))
            }
            // A picker that was closed, or opened on another chain, in the meantime is left alone.
            update { state ->
                state.copy(venuePicker = state.venuePicker?.let { if (it.chain == chain) it.copy(catalog = catalogUi) else it })
            }
        }
    }

    fun search(query: String) {
        updatePicker { it.copy(query = query) }
    }

    fun toggleDraft(venue: Venue) {
        updatePicker { it.copy(draft = if (venue in it.draft) it.draft - venue else it.draft + venue) }
    }

    // "Select all" and "Deselect all" act on the venues the search leaves on screen.
    fun selectAllVisible() {
        updatePicker { it.copy(draft = it.draft + it.visibleVenues) }
    }

    fun deselectAllVisible() {
        updatePicker { it.copy(draft = it.draft - it.visibleVenues.toSet()) }
    }

    // Makes the ticked venues the selection and closes the picker. Returns true when the selection changed.
    fun confirm(): Boolean {
        var changed = false
        update { state ->
            val picker = state.venuePicker ?: return@update state
            changed = picker.draft != state.selectedVenues
            state.copy(selectedVenues = picker.draft, venuePicker = null)
        }
        return changed
    }

    fun saveSelection(venues: Set<Venue>) {
        filterPreferences.setSelectedVenues(venues)
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

    private fun updatePicker(transform: (VenuePickerUi) -> VenuePickerUi) {
        update { state -> state.copy(venuePicker = state.venuePicker?.let(transform)) }
    }
}

package com.preichert.cinemerick.feature.showtimes.presentation

import co.touchlab.kermit.Logger
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.tomorrow
import com.preichert.cinemerick.feature.showtimes.domain.upcomingWeekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copied
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val log = Logger.withTag("ShowtimesViewModel")

class ShowtimesViewModel(
    private val showtimesRepository: ShowtimesRepository,
    private val filmDetailsRepository: FilmDetailsRepository,
    private val filterPreferences: FilterPreferences,
    private val timeWindowController: TimeWindowController,
    private val personDataSource: PersonRemoteDataSource,
    private val clock: Clock = Clock.System,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val filterPersistence = FilterPersistence(savedStateHandle)
    private val _state = MutableStateFlow(initialState())
    val state = _state.asStateFlow()

    // Events are few and user-triggered, so they are queued until the screen collects them instead of being dropped.
    private val _events = Channel<ShowtimesEvent>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private val days = DaysController(timeWindowController, ::today) { change -> _state.update { change(it) } }
    private val venuePicker = VenuePickerController(viewModelScope, showtimesRepository, filterPreferences) { change -> _state.update { change(it) } }
    private val results = ResultsController(viewModelScope, showtimesRepository, filmDetailsRepository, clock, ::now, { _state.value }, { change -> _state.update { change(it) } }, ::showSnackbar)
    private val cardLoader = CardDetailsLoader(viewModelScope, filmDetailsRepository, ::applyCardDetails)
    private val filmDetails = FilmDetailsLoader(viewModelScope, filmDetailsRepository) { change -> _state.update { change(it) } }
    private val persons = PersonDetailsLoader(viewModelScope, personDataSource) { change -> _state.update { change(it) } }

    init {
        filterPersistence.track(viewModelScope, _state)
    }

    fun onAction(action: ShowtimesAction) {
        when (action) {
            is ShowtimesAction.OnDayToggle -> { days.toggle(action.date); refresh() }
            ShowtimesAction.OnDatesConfirm -> if (days.confirm()) refresh()
            is ShowtimesAction.OnCalendarDayToggle -> days.toggleDraft(action.date)
            ShowtimesAction.OnCalendarClear -> days.clearDraft()
            is ShowtimesAction.OnCalendarMonthChange -> days.showMonth(action.month)
            ShowtimesAction.OnCalendarOpen -> days.openCalendar()
            ShowtimesAction.OnCalendarDismiss -> days.dismissCalendar()
            ShowtimesAction.OnSelectToday -> { days.select(listOf(today())); refresh() }
            ShowtimesAction.OnSelectTomorrow -> { days.select(listOf(tomorrow(today()))); refresh() }
            ShowtimesAction.OnSelectWeekend -> { days.select(upcomingWeekend(today())); refresh() }
            is ShowtimesAction.OnMinTimeChange -> { days.setMinTime(action.date, action.value); refresh() }
            is ShowtimesAction.OnMaxTimeChange -> { days.setMaxTime(action.date, action.value); refresh() }
            is ShowtimesAction.OnFilmFilterChange -> _state.update { it.copy(filmFilter = action.value) }
            is ShowtimesAction.OnVenueToggle -> { venuePicker.toggle(action.venue); refresh() }
            is ShowtimesAction.OnVenuePickerOpen -> venuePicker.open(action.chain)
            ShowtimesAction.OnVenuePickerDismiss -> venuePicker.dismiss()
            is ShowtimesAction.OnVenueSearchChange -> venuePicker.search(action.query)
            is ShowtimesAction.OnVenueDraftToggle -> venuePicker.toggleDraft(action.venue)
            ShowtimesAction.OnVenueDraftSelectAll -> venuePicker.selectAllVisible()
            ShowtimesAction.OnVenueDraftDeselectAll -> venuePicker.deselectAllVisible()
            ShowtimesAction.OnVenuePickerConfirm -> confirmVenues()
            is ShowtimesAction.OnChainExpandToggle -> toggleChain(action.chain)
            is ShowtimesAction.OnFormatToggle -> toggleFormat(action.tag)
            ShowtimesAction.OnFormatsReset -> setHiddenTags(emptySet())
            ShowtimesAction.OnGenerateClick -> results.generate(silent = false)
            ShowtimesAction.OnCopyClick -> copy()
            ShowtimesAction.OnClearClick -> clear()
            ShowtimesAction.OnVenuesReload -> venuePicker.reload()
            is ShowtimesAction.OnFilmCardVisible -> cardLoader.load(action.title, resolved = action.title in _state.value.resolvedCards)
            is ShowtimesAction.OnFilmDetailsOpen -> _state.value.groups.firstOrNull { it.title == action.title }?.let(filmDetails::open)
            ShowtimesAction.OnFilmDetailsDismiss -> filmDetails.dismiss()
            ShowtimesAction.OnSynopsisExpand -> updateFilmDetails { it.copy(isSynopsisExpanded = true) }
            is ShowtimesAction.OnFilmImageOpen -> updateFilmDetails { it.copy(openedImage = action.image) }
            ShowtimesAction.OnFilmImageDismiss -> updateFilmDetails { it.copy(openedImage = null) }
            is ShowtimesAction.OnPosterEnlarge -> _state.update { it.copy(enlargedPoster = PosterUi(action.title, action.url)) }
            ShowtimesAction.OnPosterDismiss -> _state.update { it.copy(enlargedPoster = null) }
            is ShowtimesAction.OnPersonOpen -> persons.open(action.person)
            ShowtimesAction.OnPersonDismiss -> persons.dismiss()
            ShowtimesAction.OnBiographyExpand -> _state.update { it.copy(personDetails = it.personDetails?.copy(isBiographyExpanded = true)) }
            ShowtimesAction.OnFilterSummaryToggle -> _state.update { it.copy(isFilterSummaryExpanded = !it.isFilterSummaryExpanded) }
            ShowtimesAction.OnFilterSummaryDismiss -> _state.update { it.copy(isFilterSummaryExpanded = false) }
            is ShowtimesAction.OnFilterSummaryTargetClick -> jumpToFilter(action.target)
            ShowtimesAction.OnFilterSummaryUpdateClick -> {
                _state.update { it.copy(isFilterSummaryExpanded = false) }
                results.generate(silent = false)
            }
        }
    }

    private fun refresh() = results.refresh()

    // The selection is saved and the results refreshed only when the picker changed it.
    private fun confirmVenues() {
        if (!venuePicker.confirm()) return
        venuePicker.saveSelection(_state.value.selectedVenues)
        refresh()
    }

    // The panel closes and a chain card opens before the screen scrolls to it, so it arrives already expanded.
    private fun jumpToFilter(target: FilterTarget) {
        _state.update { state ->
            state.copy(
                isFilterSummaryExpanded = false,
                expandedChains = if (target is FilterTarget.ChainCard) state.expandedChains + target.chain else state.expandedChains,
            )
        }
        _events.trySend(ShowtimesEvent.ScrollToFilter(target))
    }

    private fun toggleChain(chain: Chain) {
        _state.update { it.copy(expandedChains = if (chain in it.expandedChains) it.expandedChains - chain else it.expandedChains + chain) }
    }

    private fun updateFilmDetails(transform: (FilmDetailsUi) -> FilmDetailsUi) {
        _state.update { it.copy(filmDetails = it.filmDetails?.let(transform)) }
    }

    private fun toggleFormat(tag: String) {
        val hidden = _state.value.hiddenTags
        setHiddenTags(if (tag in hidden) hidden - tag else hidden + tag)
    }

    private fun setHiddenTags(tags: Set<String>) {
        _state.update { results.withResults(it.copy(hiddenTags = tags)) }
    }

    private fun initialState(): ShowtimesState =
        filterPersistence.restore(freshState(selectedVenues = filterPreferences.getSelectedVenues() ?: defaultVenues))

    private fun freshState(selectedVenues: Set<Venue>) = ShowtimesState(
        selectedVenues = selectedVenues
    )

    private companion object {
        val defaultVenues = setOf(
            Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto"),
            Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto"),
            Venue(Chain.NOTORIOUS, "fiumeveneto", "Fiume Veneto", "Friuli", "https://www.notoriouscinemas.it/fiumeveneto/index.php")
        )
    }

    // Ignored while loading: the in-flight generate() would write its results back afterwards.
    private fun clear() {
        if (_state.value.isLoading) return
        results.clear()
        filterPreferences.setSelectedVenues(emptySet())
        _state.value = freshState(selectedVenues = emptySet())
    }

    private fun copy() {
        val text = _state.value.pollText
        if (text.isBlank()) return
        _events.trySend(ShowtimesEvent.CopyToClipboard(text))
        _events.trySend(ShowtimesEvent.ShowSnackbar(UiText.Resource(Res.string.copied)))
    }

    private fun applyCardDetails(answers: Map<String, FilmDetails?>) {
        _state.update { state ->
            results.withResults(
                state.copy(
                    cardDetails = state.cardDetails + answers.mapNotNull { (title, details) -> details?.let { title to it } },
                    resolvedCards = state.resolvedCards + answers.keys,
                )
            )
        }
    }

    private fun showSnackbar(message: UiText) {
        _events.trySend(ShowtimesEvent.ShowSnackbar(message))
    }

    private fun today(): LocalDate = now().date

    private fun now(): LocalDateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())

}

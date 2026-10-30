package com.preichert.cinemerick.feature.showtimes.presentation

import co.touchlab.kermit.Logger
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.toTimeInput
import com.preichert.cinemerick.core.presentation.toUiText
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.tomorrow
import com.preichert.cinemerick.feature.showtimes.domain.upcomingWeekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copied
import com.preichert.cinemerick.feature.showtimes.presentation.resources.venues_load_error
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

// The filters that survive process death. They are saved as JSON, so restoring them does not depend on the platform.
@Serializable
private data class SavedFilters(
    val days: List<SavedDay> = emptyList(),
    val filmFilter: String = "",
    val hiddenTags: List<String> = emptyList(),
)

@Serializable
private data class SavedDay(val date: String, val isSelected: Boolean, val minTime: String, val maxTime: String)

private val log = Logger.withTag("ShowtimesViewModel")

class ShowtimesViewModel(
    private val showtimesRepository: ShowtimesRepository,
    private val filmDetailsRepository: FilmDetailsRepository,
    private val filterPreferences: FilterPreferences,
    private val timeWindowController: TimeWindowController,
    private val personLookup: PersonLookup,
    private val clock: Clock = Clock.System,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

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
    private val persons = PersonDetailsLoader(viewModelScope, personLookup) { change -> _state.update { change(it) } }

    init {
        // Every filter change is saved, so a restored process starts from the same days, times and tags.
        viewModelScope.launch {
            _state.map { it.toSavedFilters() }.distinctUntilChanged().collect { saved ->
                savedStateHandle[FILTERS_KEY] = Json.encodeToString(SavedFilters.serializer(), saved)
            }
        }
    }

    fun onAction(action: ShowtimesAction) {
        when (action) {
            is ShowtimesAction.OnDayToggle -> { days.toggle(action.date); refresh() }
            is ShowtimesAction.OnDatesConfirm -> { days.confirm(action.dates); refresh() }
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
            is ShowtimesAction.OnFormatToggle -> toggleFormat(action.tag)
            ShowtimesAction.OnFormatsReset -> setHiddenTags(emptySet())
            ShowtimesAction.OnGenerateClick -> results.generate(silent = false)
            ShowtimesAction.OnCopyClick -> copy()
            ShowtimesAction.OnClearClick -> clear()
            ShowtimesAction.OnVenuesReload -> {
                val chain = _state.value.venuePicker?.chain
                if (chain != null) venuePicker.open(chain)
            }
            is ShowtimesAction.OnFilmCardVisible -> cardLoader.load(action.title, resolved = action.title in _state.value.resolvedCards)
            is ShowtimesAction.OnFilmDetailsOpen -> _state.value.groups.firstOrNull { it.title == action.title }?.let(filmDetails::open)
            ShowtimesAction.OnFilmDetailsDismiss -> filmDetails.dismiss()
            is ShowtimesAction.OnPersonOpen -> persons.open(action.person)
            ShowtimesAction.OnPersonDismiss -> persons.dismiss()
        }
    }

    private fun refresh() = results.refresh()

    private fun toggleFormat(tag: String) {
        val hidden = _state.value.hiddenTags
        setHiddenTags(if (tag in hidden) hidden - tag else hidden + tag)
    }

    private fun setHiddenTags(tags: Set<String>) {
        _state.update { results.withResults(it.copy(hiddenTags = tags)) }
    }

    private fun initialState(): ShowtimesState {
        val saved = restoredFilters()
        return freshState(selectedVenues = filterPreferences.getSelectedVenues() ?: defaultVenues).copy(
            days = saved.days.mapNotNull { day ->
                runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { date ->
                    DayUi(date, day.isSelected, day.minTime, day.maxTime)
                }
            },
            filmFilter = saved.filmFilter,
            hiddenTags = saved.hiddenTags.toSet(),
        )
    }

    // A saved state that cannot be read is ignored: the filters then start from their defaults.
    private fun restoredFilters(): SavedFilters = runCatching {
        savedStateHandle.get<String>(FILTERS_KEY)?.let { Json.decodeFromString(SavedFilters.serializer(), it) }
    }.getOrNull() ?: SavedFilters()

    private fun ShowtimesState.toSavedFilters() = SavedFilters(
        days = days.map { SavedDay(it.date.toString(), it.isSelected, it.minTime, it.maxTime) },
        filmFilter = filmFilter,
        hiddenTags = hiddenTags.sorted(),
    )

    private fun freshState(selectedVenues: Set<Venue>) = ShowtimesState(
        selectedVenues = selectedVenues
    )

    private companion object {
        const val FILTERS_KEY = "filters"
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

package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.toUiText
import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.DayRange
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.ParsedTime
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.availableTags
import com.preichert.cinemerick.feature.showtimes.domain.displayFormat
import com.preichert.cinemerick.feature.showtimes.domain.filterShowings
import com.preichert.cinemerick.feature.showtimes.domain.groupByFilm
import com.preichert.cinemerick.feature.showtimes.domain.italianName
import com.preichert.cinemerick.feature.showtimes.domain.parseTimeInput
import com.preichert.cinemerick.feature.showtimes.domain.toPollText
import com.preichert.cinemerick.feature.showtimes.domain.upcomingWeekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copied
import com.preichert.cinemerick.feature.showtimes.presentation.resources.invalid_time
import com.preichert.cinemerick.feature.showtimes.presentation.resources.select_day
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val log = Logger.withTag("ShowtimesViewModel")

class ShowtimesViewModel(
    private val showtimesRepository: ShowtimesRepository,
    private val filterPreferences: FilterPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state = _state.asStateFlow()

    private val _events = Channel<ShowtimesEvent>()
    val events = _events.receiveAsFlow()

    // Last fetched results, kept so format filters apply instantly without refetching.
    private var lastFetch: Fetch? = null
    private var generateJob: Job? = null

    private class Fetch(val showings: List<Showing>, val ranges: List<DayRange>, val filmQueries: List<String>)

    fun onAction(action: ShowtimesAction) {
        when (action) {
            is ShowtimesAction.OnDayToggle -> toggleDay(action.date)
            ShowtimesAction.OnCalendarOpen -> _state.update { it.copy(calendar = CalendarUi(minDate = today())) }
            ShowtimesAction.OnCalendarDismiss -> _state.update { it.copy(calendar = null) }
            ShowtimesAction.OnSelectToday -> selectDays(listOf(today()))
            ShowtimesAction.OnSelectWeekend -> selectDays(upcomingWeekend(today()))
            is ShowtimesAction.OnMinTimeChange -> updateDay(action.date) { it.copy(minTime = action.value) }
            is ShowtimesAction.OnMaxTimeChange -> updateDay(action.date) { it.copy(maxTime = action.value) }
            is ShowtimesAction.OnFilmFilterChange -> _state.update { it.copy(filmFilter = action.value) }
            is ShowtimesAction.OnCinemaToggle -> toggleCinema(action.cinema)
            is ShowtimesAction.OnFormatToggle -> toggleFormat(action.tag)
            ShowtimesAction.OnFormatsReset -> setHiddenTags(emptySet())
            ShowtimesAction.OnGenerateClick -> generate(silent = false)
            ShowtimesAction.OnCopyClick -> copy()
            ShowtimesAction.OnClearClick -> clear()
        }
    }

    // Keeps the time ranges of dates that stay selected.
    private fun selectDays(dates: List<LocalDate>) {
        _state.update { state ->
            state.copy(days = dates.map { date -> state.days.firstOrNull { it.date == date } ?: newDay(date) })
        }
    }

    private fun toggleDay(date: LocalDate) {
        _state.update { state ->
            val days = if (state.days.any { it.date == date }) {
                state.days.filterNot { it.date == date }
            } else {
                (state.days + newDay(date)).sortedBy { it.date }
            }
            state.copy(days = days)
        }
    }

    // At least one cinema must stay selected.
    private fun toggleCinema(cinema: Cinema) {
        _state.update { state ->
            val cinemas = if (cinema in state.selectedCinemas) state.selectedCinemas - cinema else state.selectedCinemas + cinema
            if (cinemas.isEmpty()) {
                state
            } else {
                filterPreferences.setSelectedCinemas(cinemas)
                state.copy(selectedCinemas = cinemas)
            }
        }
        // Results are already on screen: quietly refresh them for the new cinema selection.
        if (_state.value.hasGenerated) generate(silent = true)
    }

    private fun toggleFormat(tag: String) {
        val hidden = _state.value.hiddenTags
        setHiddenTags(if (tag in hidden) hidden - tag else hidden + tag)
    }

    private fun setHiddenTags(tags: Set<String>) {
        filterPreferences.setHiddenTags(tags)
        _state.update { it.copy(hiddenTags = tags).withResults(lastFetch) }
    }

    private fun initialState() = ShowtimesState(
        hiddenTags = filterPreferences.getHiddenTags(),
        selectedCinemas = filterPreferences.getSelectedCinemas() ?: Cinema.entries.toSet()
    )
        .let { it.copy(availableTags = it.hiddenTags.sorted()) }

    private fun newDay(date: LocalDate) = DayUi(date = date, label = date.toLabel(), isSelected = true)

    private fun updateDay(date: LocalDate, transform: (DayUi) -> DayUi) {
        _state.update { state ->
            state.copy(days = state.days.map { if (it.date == date) transform(it) else it })
        }
    }

    // Ignored while loading: the in-flight generate() would write its results back afterwards.
    private fun clear() {
        if (_state.value.isLoading) return
        lastFetch = null
        _state.value = initialState()
    }

    private fun copy() {
        val text = _state.value.pollText
        if (text.isBlank()) return
        viewModelScope.launch {
            _events.send(ShowtimesEvent.CopyToClipboard(text))
            _events.send(ShowtimesEvent.ShowSnackbar(UiText.Resource(Res.string.copied)))
        }
    }

    // A silent run (automatic refresh) skips validation messages; a newer run cancels the one in flight.
    private fun generate(silent: Boolean) {
        val selectedDays = _state.value.days.filter { it.isSelected }
        if (selectedDays.isEmpty()) {
            if (!silent) showSnackbar(UiText.Resource(Res.string.select_day))
            return
        }
        val ranges = selectedDays.map { day ->
            val min = parseTimeInput(day.minTime)
            val max = parseTimeInput(day.maxTime)
            if (min is ParsedTime.Invalid || max is ParsedTime.Invalid) {
                if (!silent) showSnackbar(UiText.Resource(Res.string.invalid_time))
                return
            }
            DayRange(day.date, (min as ParsedTime.Valid).value, (max as ParsedTime.Valid).value)
        }
        val filmQueries = _state.value.filmFilter.split(",")

        generateJob?.cancel()
        generateJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val results = showtimesRepository.getShowings(ranges.map { it.date }, _state.value.selectedCinemas)
            val showings = mutableListOf<Showing>()
            val errors = mutableListOf<CinemaErrorUi>()
            results.forEach { cinemaShowings ->
                when (val result = cinemaShowings.result) {
                    is Result.Success -> showings += result.data
                    is Result.Error -> errors += CinemaErrorUi(
                        cinemaName = cinemaShowings.cinema.displayName,
                        message = result.error.toUiText()
                    )
                }
            }

            lastFetch = Fetch(showings, ranges, filmQueries)
            log.i { "Fetched ${showings.size} showing(s), ${errors.size} cinema error(s)" }
            _state.update {
                it.copy(isLoading = false, hasGenerated = true, cinemaErrors = errors).withResults(lastFetch)
            }
        }
    }

    // Applies the film/time filters plus the hidden format tags to the last fetched showings.
    private fun ShowtimesState.withResults(fetch: Fetch?): ShowtimesState {
        if (fetch == null) return this
        val now = now()
        val groups = fetch.showings
            .filterShowings(fetch.ranges, fetch.filmQueries, now, hiddenTags)
            .groupByFilm()
        // Tags come from the unfiltered-by-format results, so hidden ones stay toggleable.
        val tags = fetch.showings.filterShowings(fetch.ranges, fetch.filmQueries, now).availableTags()
        return copy(
            groups = groups.map { group ->
                FilmGroupUi(
                    title = group.title,
                    posterUrl = group.posterUrl,
                    showings = group.showings.map { showing ->
                        ShowingUi(
                            day = showing.day.dayOfWeek.italianName(),
                            time = showing.time.toString(),
                            cinema = showing.cinema,
                            format = showing.displayFormat
                        )
                    }
                )
            },
            pollText = groups.toPollText(),
            optionsCount = groups.sumOf { group -> group.showings.size },
            availableTags = (tags + hiddenTags).distinct().sorted()
        )
    }

    private fun showSnackbar(message: UiText) {
        viewModelScope.launch { _events.send(ShowtimesEvent.ShowSnackbar(message)) }
    }

    private fun today(): LocalDate = now().date

    private fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun LocalDate.toLabel(): String {
        val weekday = dayOfWeek.italianName().take(3).lowercase()
        return "$weekday ${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"
    }
}

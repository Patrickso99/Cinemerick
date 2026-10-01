package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.toUiText
import com.preichert.cinemerick.feature.showtimes.domain.DayRange
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.filterShowings
import com.preichert.cinemerick.feature.showtimes.domain.groupByFilm
import com.preichert.cinemerick.feature.showtimes.domain.italianName
import com.preichert.cinemerick.feature.showtimes.domain.toPollLine
import com.preichert.cinemerick.feature.showtimes.domain.toPollText
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copied
import com.preichert.cinemerick.feature.showtimes.presentation.resources.invalid_time
import com.preichert.cinemerick.feature.showtimes.presentation.resources.select_day
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ShowtimesViewModel(
    private val showtimesRepository: ShowtimesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ShowtimesState(days = upcomingDays()))
    val state = _state.asStateFlow()

    private val _events = Channel<ShowtimesEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ShowtimesAction) {
        when (action) {
            is ShowtimesAction.OnDayToggle -> updateDay(action.date) { it.copy(isSelected = !it.isSelected) }
            is ShowtimesAction.OnMinTimeChange -> updateDay(action.date) { it.copy(minTime = action.value) }
            is ShowtimesAction.OnMaxTimeChange -> updateDay(action.date) { it.copy(maxTime = action.value) }
            is ShowtimesAction.OnFilmFilterChange -> _state.update { it.copy(filmFilter = action.value) }
            ShowtimesAction.OnGenerateClick -> generate()
            ShowtimesAction.OnCopyClick -> copy()
        }
    }

    private fun updateDay(date: LocalDate, transform: (DayUi) -> DayUi) {
        _state.update { state ->
            state.copy(days = state.days.map { if (it.date == date) transform(it) else it })
        }
    }

    private fun copy() {
        val text = _state.value.pollText
        if (text.isBlank()) return
        viewModelScope.launch {
            _events.send(ShowtimesEvent.CopyToClipboard(text))
            _events.send(ShowtimesEvent.ShowSnackbar(UiText.Resource(Res.string.copied)))
        }
    }

    private fun generate() {
        val selectedDays = _state.value.days.filter { it.isSelected }
        if (selectedDays.isEmpty()) {
            showSnackbar(UiText.Resource(Res.string.select_day))
            return
        }
        val ranges = selectedDays.map { day ->
            val min = parseTime(day.minTime)
            val max = parseTime(day.maxTime)
            if (min is ParsedTime.Invalid || max is ParsedTime.Invalid) {
                showSnackbar(UiText.Resource(Res.string.invalid_time))
                return
            }
            DayRange(day.date, (min as ParsedTime.Valid).value, (max as ParsedTime.Valid).value)
        }
        val filmQueries = _state.value.filmFilter.split(",")

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val results = showtimesRepository.getShowings(ranges.map { it.date })
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

            val groups = showings
                .filterShowings(ranges, filmQueries, now())
                .groupByFilm()

            _state.update {
                it.copy(
                    isLoading = false,
                    hasGenerated = true,
                    groups = groups.map { group ->
                        FilmGroupUi(group.title, group.showings.map { showing -> showing.toPollLine() })
                    },
                    pollText = groups.toPollText(),
                    optionsCount = groups.sumOf { group -> group.showings.size },
                    cinemaErrors = errors
                )
            }
        }
    }

    private fun showSnackbar(message: UiText) {
        viewModelScope.launch { _events.send(ShowtimesEvent.ShowSnackbar(message)) }
    }

    private sealed interface ParsedTime {
        data class Valid(val value: LocalTime?) : ParsedTime
        data object Invalid : ParsedTime
    }

    private fun parseTime(text: String): ParsedTime {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ParsedTime.Valid(null)
        val normalized = if (trimmed.length == 4 && trimmed[1] == ':') "0$trimmed" else trimmed
        return runCatching { LocalTime.parse(normalized) }
            .map { ParsedTime.Valid(it) }
            .getOrDefault(ParsedTime.Invalid)
    }

    private fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun upcomingDays(): List<DayUi> {
        val today = now().date
        return (0 until UPCOMING_DAYS).map { offset ->
            val date = today.plus(offset, DateTimeUnit.DAY)
            DayUi(date = date, label = date.toLabel())
        }
    }

    private fun LocalDate.toLabel(): String {
        val weekday = dayOfWeek.italianName().take(3).lowercase()
        return "$weekday ${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"
    }

    private companion object {
        const val UPCOMING_DAYS = 21
    }
}

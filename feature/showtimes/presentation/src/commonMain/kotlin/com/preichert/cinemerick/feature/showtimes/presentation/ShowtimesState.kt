package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.ParsedTime
import com.preichert.cinemerick.feature.showtimes.domain.parseTimeInput
import kotlinx.datetime.LocalDate

@Stable
data class ShowtimesState(
    val days: List<DayUi> = emptyList(),
    val calendar: CalendarUi? = null,
    val filmFilter: String = "",
    val selectedCinemas: Set<Cinema> = Cinema.entries.toSet(),
    val isLoading: Boolean = false,
    val hasGenerated: Boolean = false,
    val groups: List<FilmGroupUi> = emptyList(),
    val pollText: String = "",
    val optionsCount: Int = 0,
    val cinemaErrors: List<CinemaErrorUi> = emptyList()
) {
    val canClear: Boolean get() = !isLoading && (days.isNotEmpty() || filmFilter.isNotEmpty() || hasGenerated)
}

/** Calendar dialog state: present while the dialog is open. */
data class CalendarUi(val minDate: LocalDate)

data class DayUi(
    val date: LocalDate,
    val label: String,
    val isSelected: Boolean = false,
    val minTime: String = DEFAULT_MIN_TIME,
    val maxTime: String = ""
) {
    val isMinTimeError: Boolean get() = parseTimeInput(minTime) is ParsedTime.Invalid
    val isMaxTimeError: Boolean get() = parseTimeInput(maxTime) is ParsedTime.Invalid

    companion object {
        const val DEFAULT_MIN_TIME = "20:00"
    }
}

data class FilmGroupUi(
    val title: String,
    val showings: List<ShowingUi>,
    val posterUrl: String? = null
)

data class ShowingUi(
    val day: String,
    val time: String,
    val cinema: Cinema,
    val format: String? = null
)

data class CinemaErrorUi(
    val cinemaName: String,
    val message: UiText
)

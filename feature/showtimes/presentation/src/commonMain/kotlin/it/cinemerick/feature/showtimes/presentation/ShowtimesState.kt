package it.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.Stable
import it.cinemerick.core.presentation.UiText
import kotlinx.datetime.LocalDate

@Stable
data class ShowtimesState(
    val days: List<DayUi> = emptyList(),
    val filmFilter: String = "",
    val isLoading: Boolean = false,
    val hasGenerated: Boolean = false,
    val groups: List<FilmGroupUi> = emptyList(),
    val pollText: String = "",
    val optionsCount: Int = 0,
    val cinemaErrors: List<CinemaErrorUi> = emptyList()
)

data class DayUi(
    val date: LocalDate,
    val label: String,
    val isSelected: Boolean = false,
    val minTime: String = DEFAULT_MIN_TIME,
    val maxTime: String = ""
) {
    companion object {
        const val DEFAULT_MIN_TIME = "20:00"
    }
}

data class FilmGroupUi(
    val title: String,
    val lines: List<String>
)

data class CinemaErrorUi(
    val cinemaName: String,
    val message: UiText
)

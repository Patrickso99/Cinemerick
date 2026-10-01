package it.cinemerick.feature.showtimes.presentation

import kotlinx.datetime.LocalDate

sealed interface ShowtimesAction {
    data class OnDayToggle(val date: LocalDate) : ShowtimesAction
    data class OnMinTimeChange(val date: LocalDate, val value: String) : ShowtimesAction
    data class OnMaxTimeChange(val date: LocalDate, val value: String) : ShowtimesAction
    data class OnFilmFilterChange(val value: String) : ShowtimesAction
    data object OnGenerateClick : ShowtimesAction
    data object OnCopyClick : ShowtimesAction
}

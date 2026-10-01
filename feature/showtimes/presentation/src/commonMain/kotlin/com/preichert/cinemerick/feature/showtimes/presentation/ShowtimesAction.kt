package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import kotlinx.datetime.LocalDate

sealed interface ShowtimesAction {
    data class OnDayToggle(val date: LocalDate) : ShowtimesAction
    data object OnCalendarOpen : ShowtimesAction
    data object OnCalendarDismiss : ShowtimesAction
    data object OnSelectToday : ShowtimesAction
    data object OnSelectWeekend : ShowtimesAction
    data class OnMinTimeChange(val date: LocalDate, val value: String) : ShowtimesAction
    data class OnMaxTimeChange(val date: LocalDate, val value: String) : ShowtimesAction
    data class OnFilmFilterChange(val value: String) : ShowtimesAction
    data class OnCinemaToggle(val cinema: Cinema) : ShowtimesAction
    data object OnGenerateClick : ShowtimesAction
    data object OnCopyClick : ShowtimesAction
    data object OnClearClick : ShowtimesAction
}

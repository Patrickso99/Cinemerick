package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate

sealed interface ShowtimesAction {
    data class OnDayToggle(val date: LocalDate) : ShowtimesAction
    data object OnCalendarOpen : ShowtimesAction
    data object OnCalendarDismiss : ShowtimesAction
    data object OnSelectToday : ShowtimesAction
    data object OnSelectTomorrow : ShowtimesAction
    data object OnSelectWeekend : ShowtimesAction
    data class OnMinTimeChange(val date: LocalDate, val value: String) : ShowtimesAction
    data class OnMaxTimeChange(val date: LocalDate, val value: String) : ShowtimesAction
    data class OnFilmFilterChange(val value: String) : ShowtimesAction
    data class OnVenueToggle(val venue: Venue) : ShowtimesAction
    data class OnVenuePickerOpen(val chain: Chain) : ShowtimesAction
    data object OnVenuePickerDismiss : ShowtimesAction
    data class OnVenueSearchChange(val query: String) : ShowtimesAction
    data class OnFormatToggle(val tag: String) : ShowtimesAction
    data object OnFormatsReset : ShowtimesAction
    data object OnGenerateClick : ShowtimesAction
    data object OnCopyClick : ShowtimesAction
    data object OnClearClick : ShowtimesAction
    data object OnVenuesReload : ShowtimesAction
}

package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.UiText

sealed interface ShowtimesEvent {
    data class CopyToClipboard(val text: String) : ShowtimesEvent
    data class ShowSnackbar(val message: UiText) : ShowtimesEvent
}

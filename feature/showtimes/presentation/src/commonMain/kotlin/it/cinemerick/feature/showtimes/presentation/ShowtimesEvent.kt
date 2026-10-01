package it.cinemerick.feature.showtimes.presentation

import it.cinemerick.core.presentation.UiText

sealed interface ShowtimesEvent {
    data class CopyToClipboard(val text: String) : ShowtimesEvent
    data class ShowSnackbar(val message: UiText) : ShowtimesEvent
}

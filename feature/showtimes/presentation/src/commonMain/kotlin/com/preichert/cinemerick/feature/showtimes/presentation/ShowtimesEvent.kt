package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.UiText

sealed interface ShowtimesEvent {
    data class CopyToClipboard(val text: String) : ShowtimesEvent
    data class ShowSnackbar(val message: UiText) : ShowtimesEvent

    /** The voice request changed the filters: the snackbar offers to put the previous ones back. */
    data object ShowVoiceUndo : ShowtimesEvent

    /** Scroll the filters to [target] and ring it: a summary bar row was tapped. */
    data class ScrollToFilter(val target: FilterTarget) : ShowtimesEvent
}

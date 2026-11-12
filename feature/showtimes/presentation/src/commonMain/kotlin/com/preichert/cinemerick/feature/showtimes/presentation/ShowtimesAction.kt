package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.MicPermissionStatus
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate

sealed interface ShowtimesAction {
    data class OnDayToggle(val date: LocalDate) : ShowtimesAction
    data object OnDatesConfirm : ShowtimesAction
    data object OnCalendarOpen : ShowtimesAction
    data object OnCalendarDismiss : ShowtimesAction
    data class OnCalendarDayToggle(val date: LocalDate) : ShowtimesAction
    data object OnCalendarClear : ShowtimesAction
    data class OnCalendarMonthChange(val month: LocalDate) : ShowtimesAction
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
    data class OnVenueDraftToggle(val venue: Venue) : ShowtimesAction
    data object OnVenueDraftSelectAll : ShowtimesAction
    data object OnVenueDraftDeselectAll : ShowtimesAction
    data object OnVenuePickerConfirm : ShowtimesAction
    data class OnChainExpandToggle(val chain: Chain) : ShowtimesAction
    data class OnFormatToggle(val tag: String) : ShowtimesAction
    data object OnFormatsReset : ShowtimesAction
    data object OnGenerateClick : ShowtimesAction
    data object OnCopyClick : ShowtimesAction
    data object OnClearClick : ShowtimesAction
    data object OnVenuesReload : ShowtimesAction
    data class OnFilmCardVisible(val title: String) : ShowtimesAction
    data class OnFilmDetailsOpen(val title: String) : ShowtimesAction
    data object OnFilmDetailsDismiss : ShowtimesAction
    data object OnSynopsisExpand : ShowtimesAction
    data class OnFilmImageOpen(val image: FilmImage) : ShowtimesAction
    data object OnFilmImageDismiss : ShowtimesAction
    data class OnPosterEnlarge(val title: String, val url: String) : ShowtimesAction
    data object OnPosterDismiss : ShowtimesAction
    data class OnPersonOpen(val person: FilmPerson) : ShowtimesAction
    data object OnPersonDismiss : ShowtimesAction
    data object OnBiographyExpand : ShowtimesAction
    data object OnFilterSummaryToggle : ShowtimesAction
    data object OnFilterSummaryDismiss : ShowtimesAction
    data class OnFilterSummaryTargetClick(val target: FilterTarget) : ShowtimesAction
    data object OnFilterSummaryUpdateClick : ShowtimesAction

    // Voice assistant
    data class OnAssistantClick(val mic: MicPermissionStatus) : ShowtimesAction
    data class OnVoiceMicResult(val status: MicPermissionStatus) : ShowtimesAction
    data object OnVoiceListen : ShowtimesAction
    data object OnVoiceStopListening : ShowtimesAction
    data object OnVoiceType : ShowtimesAction
    data class OnVoiceTextChange(val text: String) : ShowtimesAction
    data object OnVoiceInterpret : ShowtimesAction
    data object OnVoiceDismiss : ShowtimesAction
    data class OnVoiceVenueConfirm(val spoken: String, val venue: Venue) : ShowtimesAction
    data class OnVoiceVenueCandidates(val spoken: String) : ShowtimesAction
    data class OnVoiceVenueIgnore(val spoken: String) : ShowtimesAction
    data class OnVoiceVenueRemove(val venue: Venue) : ShowtimesAction
    data class OnVoiceDayRemove(val date: LocalDate) : ShowtimesAction
    data object OnVoiceFormatsRemove : ShowtimesAction
    data class OnVoiceFilmRemove(val title: String) : ShowtimesAction
    data object OnVoiceSearch : ShowtimesAction
    data object OnVoiceUndo : ShowtimesAction
    data object OnSystemAiDownload : ShowtimesAction
}

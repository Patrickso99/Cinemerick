package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.Immutable
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.feature.showtimes.domain.Venue

/** The voice sheet, from asking for the microphone to the review the user searches from. Null while it is closed. */
@Immutable
sealed interface VoiceUi {
    /** No engine is set up: the sheet explains how to connect one. */
    data object Setup : VoiceUi

    /** Before the system asks for the microphone. */
    data object MicRationale : VoiceUi

    /** The microphone was refused for good: the request can still be typed. */
    data class MicDenied(val text: String) : VoiceUi

    data class Listening(val transcript: String = "", val level: Float = 0f) : VoiceUi

    /** Listening ended without a word. */
    data object NothingHeard : VoiceUi

    data class Typing(val text: String) : VoiceUi

    data class Interpreting(val text: String) : VoiceUi

    data class Review(val text: String, val review: VoiceReviewUi) : VoiceUi

    /** The request was read but named nothing to search for. */
    data class NotUnderstood(val text: String) : VoiceUi

    data class Failed(val text: String, val error: AssistantError) : VoiceUi
}

/**
 * What a request changes, group by group. A null group was not mentioned and keeps the filter as it is. Cinemas the
 * assistant is not sure about are [questions]; cinemas with no near name are [notFound] and are skipped.
 */
@Immutable
data class VoiceReviewUi(
    val venues: List<Venue>? = null,
    val questions: List<VenueQuestionUi> = emptyList(),
    val notFound: List<String> = emptyList(),
    val days: List<DayUi>? = null,
    val formats: FormatChoiceUi? = null,
    val films: List<String>? = null,
) {
    val venuesMentioned: Boolean get() = venues != null
}

/** A cinema to confirm: [best] is the likely one, [candidates] the alternatives offered when the user asks. */
@Immutable
data class VenueQuestionUi(
    val spoken: String,
    val best: Venue,
    val candidates: List<Venue>,
    val showCandidates: Boolean = false,
)

/** The formats the user asked for or refused, and the tags that hides. */
@Immutable
data class FormatChoiceUi(
    val only: List<String>,
    val excluded: List<String>,
    val hiddenTags: Set<String>,
)

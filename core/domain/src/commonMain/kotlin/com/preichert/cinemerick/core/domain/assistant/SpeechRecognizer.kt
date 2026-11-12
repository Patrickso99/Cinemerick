package com.preichert.cinemerick.core.domain.assistant

import kotlinx.coroutines.flow.Flow

/** Turns what the user says into text, on the device. */
interface SpeechRecognizer {
    /** False where the platform has no speech recognition (desktop): the voice sheet then opens for typing. */
    val isAvailable: Boolean

    /**
     * Listens in [languageTag] (BCP 47) until the user stops talking, emitting the text as it is recognized. The flow
     * completes after [SpeechEvent.Final] or [SpeechEvent.Failed]; cancelling it stops the microphone.
     */
    fun listen(languageTag: String): Flow<SpeechEvent>
}

sealed interface SpeechEvent {
    /** The microphone is open. */
    data object Ready : SpeechEvent
    /** Text recognized so far; it can still change. */
    data class Partial(val text: String) : SpeechEvent
    /** Voice level, 0 to 1, to animate the listening circle. */
    data class Level(val value: Float) : SpeechEvent
    data class Final(val text: String) : SpeechEvent
    data class Failed(val error: SpeechError) : SpeechEvent
}

enum class SpeechError {
    /** Nothing was said before the timeout. */
    NO_SPEECH,
    /** The microphone or speech permission is denied. */
    PERMISSION,
    /** The recognizer is missing or busy on this device. */
    UNAVAILABLE,
    UNKNOWN,
}

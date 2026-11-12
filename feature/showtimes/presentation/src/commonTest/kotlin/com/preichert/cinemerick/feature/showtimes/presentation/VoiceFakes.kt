package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.SpeechEvent
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import com.preichert.cinemerick.core.presentation.LanguageController
import com.preichert.cinemerick.core.testing.FakeAssistantRepository
import com.preichert.cinemerick.core.testing.FakeLanguagePreferences
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequest
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequestInterpreter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

/** Speech that emits [events] and then, unless [keepOpen], ends; with [keepOpen] it waits for [live] like a real microphone. */
internal class FakeSpeechRecognizer(
    override var isAvailable: Boolean = true,
    var events: List<SpeechEvent> = emptyList(),
    var keepOpen: Boolean = false,
) : SpeechRecognizer {
    val live = MutableSharedFlow<SpeechEvent>()
    var lastLanguageTag: String? = null

    override fun listen(languageTag: String): Flow<SpeechEvent> = flow {
        lastLanguageTag = languageTag
        events.forEach { emit(it) }
        if (keepOpen) live.collect { emit(it) }
    }
}

/** Answers [result], or suspends on [answer] when it is set, so a test can act while the request is being read. */
internal class FakeCinemaRequestInterpreter(
    var result: Result<CinemaRequest, AssistantError> = Result.Success(CinemaRequest()),
) : CinemaRequestInterpreter {
    var calls = 0
    var lastText: String? = null
    var answer: kotlinx.coroutines.CompletableDeferred<Result<CinemaRequest, AssistantError>>? = null

    override suspend fun interpret(text: String, today: LocalDate): Result<CinemaRequest, AssistantError> {
        calls++
        lastText = text
        return answer?.await() ?: result
    }
}

/** The voice collaborators of the view model, for tests that do not use the assistant. */
internal class VoiceFakes(
    val speech: FakeSpeechRecognizer = FakeSpeechRecognizer(),
    val interpreter: FakeCinemaRequestInterpreter = FakeCinemaRequestInterpreter(),
    val assistant: FakeAssistantRepository = FakeAssistantRepository(),
    val language: LanguageController = LanguageController(FakeLanguagePreferences()),
)

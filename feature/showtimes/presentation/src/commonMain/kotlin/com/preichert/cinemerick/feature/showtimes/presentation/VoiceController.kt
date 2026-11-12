package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.core.domain.assistant.AssistantStatus
import com.preichert.cinemerick.core.domain.assistant.SpeechError
import com.preichert.cinemerick.core.domain.assistant.SpeechEvent
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import com.preichert.cinemerick.core.presentation.MicPermissionStatus
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequestInterpreter
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * The voice sheet: listening, reading the request with the assistant and the review the user searches from. Only one
 * thing runs at a time: a new step cancels the listening or the reading in flight. Only used on the main thread, like
 * the view model.
 */
internal class VoiceController(
    private val scope: CoroutineScope,
    private val speech: SpeechRecognizer,
    private val interpreter: CinemaRequestInterpreter,
    private val assistant: AssistantRepository,
    private val showtimesRepository: ShowtimesRepository,
    private val languageTag: () -> String,
    private val today: () -> LocalDate,
    private val newDay: (LocalDate) -> DayUi,
    private val read: () -> ShowtimesState,
    private val update: StateUpdate,
    private val search: (VoiceReviewUi) -> Unit,
) {
    private var job: Job? = null

    /** The assistant button: set up first, then the microphone, then listening (or typing where there is no speech). */
    fun open(mic: MicPermissionStatus) {
        when {
            assistant.status.value is AssistantStatus.NeedsSetup -> show(VoiceUi.Setup)
            !speech.isAvailable -> show(VoiceUi.Typing(""))
            mic == MicPermissionStatus.GRANTED -> listen()
            mic == MicPermissionStatus.BLOCKED -> show(VoiceUi.MicDenied(""))
            else -> show(VoiceUi.MicRationale)
        }
    }

    fun onMicResult(status: MicPermissionStatus) {
        if (status == MicPermissionStatus.GRANTED) listen() else show(VoiceUi.MicDenied(""))
    }

    fun listen() {
        job?.cancel()
        show(VoiceUi.Listening())
        job = scope.launch {
            speech.listen(languageTag()).collect { event ->
                when (event) {
                    SpeechEvent.Ready -> Unit
                    is SpeechEvent.Partial -> updateListening { it.copy(transcript = event.text) }
                    is SpeechEvent.Level -> updateListening { it.copy(level = event.value) }
                    is SpeechEvent.Final -> interpret(event.text)
                    is SpeechEvent.Failed -> show(
                        when (event.error) {
                            SpeechError.NO_SPEECH -> VoiceUi.NothingHeard
                            SpeechError.PERMISSION -> VoiceUi.MicDenied("")
                            SpeechError.UNAVAILABLE, SpeechError.UNKNOWN -> VoiceUi.Typing("")
                        }
                    )
                }
            }
        }
    }

    // The user is done: what was heard so far is read at once.
    fun stopListening() {
        val transcript = (read().voice as? VoiceUi.Listening)?.transcript.orEmpty()
        job?.cancel()
        if (transcript.isBlank()) show(VoiceUi.NothingHeard) else interpret(transcript)
    }

    // The text heard or read so far is kept, so the user corrects it instead of starting over.
    fun type() {
        job?.cancel()
        show(VoiceUi.Typing(currentText()))
    }

    fun changeText(text: String) {
        update { state ->
            state.copy(
                voice = when (val voice = state.voice) {
                    is VoiceUi.MicDenied -> voice.copy(text = text)
                    is VoiceUi.Typing -> voice.copy(text = text)
                    else -> voice
                }
            )
        }
    }

    fun interpret(text: String = currentText()) {
        job?.cancel()
        if (text.isBlank()) {
            show(VoiceUi.NotUnderstood(text))
            return
        }
        show(VoiceUi.Interpreting(text))
        job = scope.launch {
            // The venues load while the assistant reads the request: both are needed for the review.
            val catalog = async { showtimesRepository.getVenues() }
            val result = interpreter.interpret(text, today())
            val voice = when (result) {
                is Result.Failure ->
                    if (result.error == AssistantError.NO_ENGINE) VoiceUi.Setup else VoiceUi.Failed(text, result.error)
                is Result.Success -> {
                    val request = result.data
                    if (request.isEmpty) {
                        VoiceUi.NotUnderstood(text)
                    } else {
                        val state = read()
                        val venues = catalog.await().mapValues { (_, venues) -> (venues as? Result.Success)?.data.orEmpty() }
                        // A chain whose venues did not load still matches the venues already selected in it.
                        val withSelected = venues.toMutableMap()
                        state.selectedVenues.groupBy { it.chain }.forEach { (chain, selected) ->
                            if (withSelected[chain].isNullOrEmpty()) withSelected[chain] = selected
                        }
                        val review = buildVoiceReview(
                            request = request,
                            catalog = withSelected,
                            selectedVenues = state.selectedVenues,
                            selectedDays = state.days,
                            knownTags = state.availableTags.toSet(),
                            newDay = newDay,
                        )
                        VoiceUi.Review(text, review)
                    }
                }
            }
            show(voice)
        }
    }

    fun confirmVenue(spoken: String, venue: Venue) = updateReview { it.confirm(spoken, venue) }
    fun showCandidates(spoken: String) = updateReview { it.showCandidates(spoken) }
    fun ignoreVenue(spoken: String) = updateReview { it.ignore(spoken) }
    fun removeVenue(venue: Venue) = updateReview { it.removeVenue(venue) }
    fun removeDay(date: LocalDate) = updateReview { it.removeDay(date) }
    fun removeFormats() = updateReview { it.removeFormats() }
    fun removeFilm(title: String) = updateReview { it.removeFilm(title) }

    fun search() {
        val review = (read().voice as? VoiceUi.Review)?.review ?: return
        dismiss()
        search(review)
    }

    fun dismiss() {
        job?.cancel()
        show(null)
    }

    private fun currentText(): String = when (val voice = read().voice) {
        is VoiceUi.Listening -> voice.transcript
        is VoiceUi.Typing -> voice.text
        is VoiceUi.MicDenied -> voice.text
        is VoiceUi.Interpreting -> voice.text
        is VoiceUi.Review -> voice.text
        is VoiceUi.NotUnderstood -> voice.text
        is VoiceUi.Failed -> voice.text
        VoiceUi.Setup, VoiceUi.MicRationale, VoiceUi.NothingHeard, null -> ""
    }

    private fun show(voice: VoiceUi?) {
        update { it.copy(voice = voice) }
    }

    private fun updateListening(transform: (VoiceUi.Listening) -> VoiceUi.Listening) {
        update { state -> (state.voice as? VoiceUi.Listening)?.let { state.copy(voice = transform(it)) } ?: state }
    }

    private fun updateReview(transform: (VoiceReviewUi) -> VoiceReviewUi) {
        update { state ->
            val voice = state.voice as? VoiceUi.Review ?: return@update state
            state.copy(voice = voice.copy(review = transform(voice.review)))
        }
    }
}

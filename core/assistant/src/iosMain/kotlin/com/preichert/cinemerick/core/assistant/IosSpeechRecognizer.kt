package com.preichert.cinemerick.core.assistant

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.assistant.SpeechError
import com.preichert.cinemerick.core.domain.assistant.SpeechEvent
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDuckOthers
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.Foundation.NSLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import kotlin.coroutines.resume

private val log = Logger.withTag("IosSpeechRecognizer")

private const val END_SILENCE_MILLIS = 1500L
private const val NO_SPEECH_TIMEOUT_MILLIS = 8000L
private const val TAP_BUFFER_SIZE = 1024u

/**
 * SFSpeechRecognizer fed by the microphone. iOS does not stop by itself, so listening ends after a short silence once
 * the user has spoken, or after a longer one when nothing was said. iOS asks for the speech and microphone permissions
 * the first time.
 */
@OptIn(ExperimentalForeignApi::class)
class IosSpeechRecognizer : SpeechRecognizer {

    override val isAvailable: Boolean = true

    override fun listen(languageTag: String): Flow<SpeechEvent> = callbackFlow {
        if (!requestSpeechPermission() || !requestMicrophonePermission()) {
            send(SpeechEvent.Failed(SpeechError.PERMISSION))
            close()
            return@callbackFlow
        }
        val recognizer = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = languageTag.toLocaleIdentifier()))
        if (!recognizer.isAvailable()) {
            send(SpeechEvent.Failed(SpeechError.UNAVAILABLE))
            close()
            return@callbackFlow
        }

        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryRecord, AVAudioSessionModeMeasurement, AVAudioSessionCategoryOptionDuckOthers, null)
        session.setActive(true, withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)

        val request = SFSpeechAudioBufferRecognitionRequest().apply { shouldReportPartialResults = true }
        val engine = AVAudioEngine()
        val input = engine.inputNode
        input.installTapOnBus(0u, TAP_BUFFER_SIZE, input.outputFormatForBus(0u)) { buffer, _ ->
            buffer?.let(request::appendAudioPCMBuffer)
        }
        engine.prepare()
        if (!engine.startAndReturnError(null)) {
            input.removeTapOnBus(0u)
            send(SpeechEvent.Failed(SpeechError.UNAVAILABLE))
            close()
            return@callbackFlow
        }
        send(SpeechEvent.Ready)

        var lastText = ""
        var silenceJob: Job? = null
        fun waitForSilence(millis: Long) {
            silenceJob?.cancel()
            silenceJob = launch {
                delay(millis)
                request.endAudio()
            }
        }
        waitForSilence(NO_SPEECH_TIMEOUT_MILLIS)

        val task = recognizer.recognitionTaskWithRequest(request) { result, error ->
            if (result != null) {
                lastText = result.bestTranscription.formattedString
                if (result.isFinal()) {
                    trySend(SpeechEvent.Final(lastText))
                    close()
                } else {
                    trySend(SpeechEvent.Partial(lastText))
                    waitForSilence(END_SILENCE_MILLIS)
                }
            } else if (error != null) {
                log.w { "Recognition ended: ${error.localizedDescription}" }
                trySend(if (lastText.isNotBlank()) SpeechEvent.Final(lastText) else SpeechEvent.Failed(SpeechError.NO_SPEECH))
                close()
            }
        }

        awaitClose {
            silenceJob?.cancel()
            task.cancel()
            engine.stop()
            input.removeTapOnBus(0u)
            request.endAudio()
            session.setActive(false, withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)
        }
    }

    private suspend fun requestSpeechPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        SFSpeechRecognizer.requestAuthorization { status ->
            continuation.resume(status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized)
        }
    }

    private suspend fun requestMicrophonePermission(): Boolean = suspendCancellableCoroutine { continuation ->
        AVAudioSession.sharedInstance().requestRecordPermission { granted -> continuation.resume(granted) }
    }
}

// iOS recognizers are per region: the app languages map to the region their cinemas and users are in.
private fun String.toLocaleIdentifier(): String = when (substringBefore('-')) {
    "it" -> "it-IT"
    "en" -> "en-US"
    else -> this
}

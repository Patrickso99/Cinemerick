package com.preichert.cinemerick.core.assistant

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import com.preichert.cinemerick.core.domain.assistant.SpeechError
import com.preichert.cinemerick.core.domain.assistant.SpeechEvent
import com.preichert.cinemerick.core.domain.assistant.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import android.speech.SpeechRecognizer as PlatformRecognizer

// The recognizer reports the level in dB, roughly -2 (silence) to 10 (loud voice).
private const val MIN_RMS_DB = -2f
private const val RMS_RANGE_DB = 12f
private const val END_SILENCE_MILLIS = 1500L

/**
 * The system speech recognizer. It ends by itself after a short silence. On Android 14+ it also detects whether the
 * user speaks Italian or English; before that it listens in the app language.
 */
class AndroidSpeechRecognizer(private val context: Context) : SpeechRecognizer {

    override val isAvailable: Boolean get() = PlatformRecognizer.isRecognitionAvailable(context)

    // The platform recognizer must be created and used on the main thread.
    override fun listen(languageTag: String): Flow<SpeechEvent> = callbackFlow {
        val recognizer = PlatformRecognizer.createSpeechRecognizer(context)
        var lastText = ""
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechEvent.Ready)
            }

            override fun onRmsChanged(rmsdB: Float) {
                trySend(SpeechEvent.Level(((rmsdB - MIN_RMS_DB) / RMS_RANGE_DB).coerceIn(0f, 1f)))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults.firstResult() ?: return
                lastText = text
                trySend(SpeechEvent.Partial(text))
            }

            override fun onResults(results: Bundle?) {
                val text = results.firstResult() ?: lastText
                trySend(if (text.isBlank()) SpeechEvent.Failed(SpeechError.NO_SPEECH) else SpeechEvent.Final(text))
                close()
            }

            override fun onError(error: Int) {
                // A partial text is still worth interpreting when the recognizer gives up at the end.
                trySend(if (lastText.isNotBlank()) SpeechEvent.Final(lastText) else SpeechEvent.Failed(error.toSpeechError()))
                close()
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        recognizer.startListening(recognizerIntent(languageTag))
        awaitClose { recognizer.destroy() }
    }.flowOn(Dispatchers.Main)

    private fun recognizerIntent(languageTag: String) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, END_SILENCE_MILLIS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES, arrayListOf("it-IT", "en-US"))
        }
    }
}

private fun Bundle?.firstResult(): String? =
    this?.getStringArrayList(PlatformRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.takeIf(String::isNotBlank)

private fun Int.toSpeechError(): SpeechError = when (this) {
    PlatformRecognizer.ERROR_SPEECH_TIMEOUT, PlatformRecognizer.ERROR_NO_MATCH -> SpeechError.NO_SPEECH
    PlatformRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechError.PERMISSION
    PlatformRecognizer.ERROR_RECOGNIZER_BUSY, PlatformRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
    PlatformRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, PlatformRecognizer.ERROR_SERVER_DISCONNECTED -> SpeechError.UNAVAILABLE
    else -> SpeechError.UNKNOWN
}

package com.preichert.cinemerick.core.assistant

import co.touchlab.kermit.Logger
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiDataSource
import com.preichert.cinemerick.core.domain.assistant.SystemAiDownload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val log = Logger.withTag("GeminiNanoDataSource")

// A request is a short JSON object: far below this, and a model that rambles is cut off.
private const val MAX_OUTPUT_TOKENS = 700

/**
 * Gemini Nano through ML Kit's GenAI Prompt API, which runs it in Google's AICore app on the device. Only some phones
 * have it (recent Pixel and Galaxy models among them, with a locked bootloader): elsewhere [availability] says
 * unsupported. AICore downloads the model itself, once, when the app asks.
 */
class GeminiNanoDataSource : SystemAiDataSource {

    override val name: String = "Gemini Nano"

    private val model: GenerativeModel by lazy { Generation.getClient() }

    override suspend fun availability(): SystemAiAvailability = try {
        when (model.checkStatus()) {
            FeatureStatus.AVAILABLE -> SystemAiAvailability.AVAILABLE
            FeatureStatus.DOWNLOADABLE -> SystemAiAvailability.DOWNLOADABLE
            FeatureStatus.DOWNLOADING -> SystemAiAvailability.DOWNLOADING
            else -> SystemAiAvailability.UNSUPPORTED
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // No AICore, an old one, an unlocked bootloader: this phone cannot use it.
        log.i(e) { "Gemini Nano is not available" }
        SystemAiAvailability.UNSUPPORTED
    }

    override fun download(): Flow<SystemAiDownload> = model.download()
        .map { status ->
            when (status) {
                is DownloadStatus.DownloadProgress -> SystemAiDownload.Progress(status.totalBytesDownloaded)
                DownloadStatus.DownloadCompleted -> SystemAiDownload.Completed
                is DownloadStatus.DownloadFailed -> SystemAiDownload.Failed
                else -> SystemAiDownload.Progress(0L)
            }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            log.w(e) { "The Gemini Nano download failed" }
            emit(SystemAiDownload.Failed)
        }

    override suspend fun complete(system: String, user: String): Result<String, AssistantError> = try {
        // Instructions go as such where the model takes them, otherwise they lead the request.
        val request = if (model.isSystemPromptAvailable()) {
            generateContentRequest(SystemInstruction(system), TextPart(user)) { configure() }
        } else {
            generateContentRequest(TextPart("$system\n\nRequest:\n$user")) { configure() }
        }
        val text = model.generateContent(request).candidates.firstOrNull()?.text?.takeIf(String::isNotBlank)
        if (text != null) Result.Success(text) else Result.Failure(AssistantError.BAD_ANSWER)
    } catch (e: CancellationException) {
        throw e
    } catch (e: GenAiException) {
        log.w(e) { "Gemini Nano refused (code ${e.errorCode})" }
        Result.Failure(e.toAssistantError())
    } catch (e: Exception) {
        log.w(e) { "Gemini Nano failed" }
        Result.Failure(AssistantError.UNKNOWN)
    }

    // The same words give the same filters: no randomness.
    private fun com.google.mlkit.genai.prompt.GenerateContentRequest.Builder.configure() {
        temperature = 0f
        topK = 1
        candidateCount = 1
        maxOutputTokens = MAX_OUTPUT_TOKENS
    }
}

private fun GenAiException.toAssistantError(): AssistantError = when (errorCode) {
    GenAiException.ErrorCode.NOT_AVAILABLE,
    GenAiException.ErrorCode.NOT_SUPPORTED,
    GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE,
    GenAiException.ErrorCode.AICORE_INCOMPATIBLE -> AssistantError.NO_ENGINE
    GenAiException.ErrorCode.PER_APP_BATTERY_USE_QUOTA_EXCEEDED -> AssistantError.RATE_LIMITED
    GenAiException.ErrorCode.BUSY,
    GenAiException.ErrorCode.BACKGROUND_USE_BLOCKED -> AssistantError.UNAVAILABLE
    GenAiException.ErrorCode.REQUEST_TOO_LARGE,
    GenAiException.ErrorCode.RESPONSE_PROCESSING_ERROR,
    GenAiException.ErrorCode.RESPONSE_GENERATION_ERROR -> AssistantError.BAD_ANSWER
    else -> AssistantError.UNKNOWN
}

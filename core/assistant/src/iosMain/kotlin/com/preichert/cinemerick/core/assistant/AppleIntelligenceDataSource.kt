package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiDataSource
import com.preichert.cinemerick.core.domain.assistant.SystemAiDownload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * What the Swift side of the app gives the Kotlin one. Apple's FoundationModels is a Swift-only framework that Kotlin
 * cannot call, so the app registers two functions at start (see `registerAppleIntelligence` in `:composeApp`). Until it
 * does, or on an iOS older than 26, there is no Apple Intelligence.
 */
object AppleIntelligenceBridge {
    /** One of "available", "appleIntelligenceNotEnabled", "modelNotReady", "deviceNotEligible", "unavailable". */
    var availability: (() -> String)? = null

    /** Answers [system] and [user] to `onResult(text, errorCode)`: an error code is one of [AppleError]. */
    var respond: ((system: String, user: String, onResult: (String?, String?) -> Unit) -> Unit)? = null
}

/** The error codes the Swift side reports. */
internal object AppleError {
    const val UNAVAILABLE = "unavailable"
    const val GUARDRAIL = "guardrail"
    const val CONTEXT = "context"
    const val LANGUAGE = "language"
    const val RATE_LIMITED = "rateLimited"
}

/** Apple Intelligence (iOS 26+, on devices that have it turned on), run on the device by the system. */
class AppleIntelligenceDataSource : SystemAiDataSource {

    override val name: String = "Apple Intelligence"

    override suspend fun availability(): SystemAiAvailability {
        val read = AppleIntelligenceBridge.availability ?: return SystemAiAvailability.UNSUPPORTED
        return when (read()) {
            "available" -> SystemAiAvailability.AVAILABLE
            "appleIntelligenceNotEnabled" -> SystemAiAvailability.NEEDS_ENABLING
            "modelNotReady" -> SystemAiAvailability.NOT_READY
            else -> SystemAiAvailability.UNSUPPORTED
        }
    }

    // The system downloads its model by itself once Apple Intelligence is on: there is nothing for the app to start.
    override fun download(): Flow<SystemAiDownload> = emptyFlow()

    override suspend fun complete(system: String, user: String): Result<String, AssistantError> {
        val respond = AppleIntelligenceBridge.respond ?: return Result.Failure(AssistantError.NO_ENGINE)
        return suspendCancellableCoroutine { continuation ->
            respond(system, user) { text, error ->
                // The Swift side answers once; a second call, or one after a cancellation, is ignored.
                if (continuation.isActive) continuation.resume(toResult(text, error))
            }
        }
    }

    private fun toResult(text: String?, error: String?): Result<String, AssistantError> = when {
        error == null && !text.isNullOrBlank() -> Result.Success(text)
        error == null -> Result.Failure(AssistantError.BAD_ANSWER)
        error == AppleError.UNAVAILABLE -> Result.Failure(AssistantError.NO_ENGINE)
        error == AppleError.RATE_LIMITED -> Result.Failure(AssistantError.RATE_LIMITED)
        error == AppleError.GUARDRAIL || error == AppleError.CONTEXT || error == AppleError.LANGUAGE -> Result.Failure(AssistantError.BAD_ANSWER)
        else -> Result.Failure(AssistantError.UNKNOWN)
    }
}

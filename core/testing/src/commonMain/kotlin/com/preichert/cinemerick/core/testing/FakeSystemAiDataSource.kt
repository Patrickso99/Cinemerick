package com.preichert.cinemerick.core.testing

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiDataSource
import com.preichert.cinemerick.core.domain.assistant.SystemAiDownload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformWhile

/**
 * A system AI whose [availability] and answer a test sets. [download] emits what [downloadEvents] receives, so a test
 * decides when it makes progress and when it ends; [availabilityAfterDownload] is what the system says once it is over.
 */
class FakeSystemAiDataSource(
    override val name: String? = "Gemini Nano",
    var availability: SystemAiAvailability = SystemAiAvailability.UNSUPPORTED,
    var answer: Result<String, AssistantError> = Result.Success("{}"),
) : SystemAiDataSource {

    val downloadEvents = MutableSharedFlow<SystemAiDownload>()
    var availabilityAfterDownload: SystemAiAvailability = SystemAiAvailability.AVAILABLE
    var downloads = 0
    var lastSystem: String? = null
    var lastUser: String? = null

    override suspend fun availability(): SystemAiAvailability = availability

    override fun download(): Flow<SystemAiDownload> = flow {
        downloads++
        // Ends after Completed or Failed, like the real download, and the system then reports what is left.
        emitAll(
            downloadEvents.transformWhile { event ->
                emit(event)
                val over = event == SystemAiDownload.Completed || event == SystemAiDownload.Failed
                if (over) availability = if (event == SystemAiDownload.Completed) availabilityAfterDownload else SystemAiAvailability.DOWNLOADABLE
                !over
            }
        )
    }

    override suspend fun complete(system: String, user: String): Result<String, AssistantError> {
        lastSystem = system
        lastUser = user
        return answer
    }
}

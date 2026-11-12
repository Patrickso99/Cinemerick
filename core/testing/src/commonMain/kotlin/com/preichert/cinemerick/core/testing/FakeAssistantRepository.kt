package com.preichert.cinemerick.core.testing

import com.preichert.cinemerick.core.domain.EmptyResult
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantEngine
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.core.domain.assistant.AssistantStatus
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiState
import kotlinx.coroutines.flow.MutableStateFlow

/** An assistant that answers [answer] and accepts a key unless [saveResult] says otherwise. Starts without a key. */
class FakeAssistantRepository(
    var answer: Result<String, AssistantError> = Result.Success("{}"),
    var saveResult: EmptyResult<AssistantError> = Result.Success(Unit),
) : AssistantRepository {
    override val status = MutableStateFlow<AssistantStatus>(AssistantStatus.NeedsSetup)
    override val savedKey = MutableStateFlow<SavedKey?>(null)
    override val savedKeys = MutableStateFlow<Map<CloudProvider, SavedKey>>(emptyMap())
    override val systemAi = MutableStateFlow(SystemAiState())
    override val chosenEngine = MutableStateFlow<AssistantEngine?>(null)

    var lastSystem: String? = null
    var lastUser: String? = null
    val savedCredentials = mutableListOf<CloudCredentials>()

    /** Sets the assistant up as if a key for [provider] had been saved. */
    fun connect(provider: CloudProvider = CloudProvider.ANTHROPIC, key: String = "sk-ant-test1234") {
        val saved = SavedKey(provider, key.takeLast(4), provider.defaultModel)
        status.value = AssistantStatus.Ready(AssistantEngine.CLOUD, provider)
        savedKey.value = saved
        savedKeys.value = savedKeys.value + (provider to saved)
    }

    /** Sets the assistant up as if the AI of the system were ready and chosen. */
    fun useSystemAi(name: String = "Gemini Nano") {
        systemAi.value = SystemAiState(name, SystemAiAvailability.AVAILABLE)
        chosenEngine.value = AssistantEngine.SYSTEM
        status.value = AssistantStatus.Ready(AssistantEngine.SYSTEM)
    }

    var refreshes = 0
    var downloads = 0

    override suspend fun refreshSystemAi() {
        refreshes++
    }

    override fun downloadSystemAi() {
        downloads++
    }

    override fun selectEngine(engine: AssistantEngine) {
        chosenEngine.value = engine
        status.value = when (engine) {
            AssistantEngine.SYSTEM -> AssistantStatus.Ready(AssistantEngine.SYSTEM)
            AssistantEngine.CLOUD -> savedKey.value?.let { AssistantStatus.Ready(AssistantEngine.CLOUD, it.provider) } ?: AssistantStatus.NeedsSetup
        }
    }

    override fun activate(provider: CloudProvider) {
        val saved = savedKeys.value[provider] ?: return
        status.value = AssistantStatus.Ready(AssistantEngine.CLOUD, provider)
        savedKey.value = saved
    }

    override suspend fun complete(system: String, user: String): Result<String, AssistantError> {
        lastSystem = system
        lastUser = user
        return answer
    }

    override suspend fun saveKey(credentials: CloudCredentials): EmptyResult<AssistantError> {
        savedCredentials += credentials
        if (saveResult is Result.Success) connect(credentials.provider, credentials.apiKey)
        return saveResult
    }

    override fun removeKey() {
        savedKey.value?.let { savedKeys.value = savedKeys.value - it.provider }
        status.value = AssistantStatus.NeedsSetup
        savedKey.value = null
    }
}

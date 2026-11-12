package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.EmptyResult
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.AssistantEngine
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.AssistantPreferences
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.core.domain.assistant.AssistantStatus
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudLlmRemoteDataSource
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.core.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The assistant answered by a cloud provider with the user's own key. The active provider is in the preferences and its
 * key in the secure key store: both are read again whenever the key changes.
 */
class CloudAssistantRepository(
    private val preferences: AssistantPreferences,
    private val keys: ApiKeyLocalDataSource,
    private val remote: CloudLlmRemoteDataSource,
) : AssistantRepository {

    private val _status = MutableStateFlow<AssistantStatus>(AssistantStatus.NeedsSetup)
    override val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _savedKey = MutableStateFlow<SavedKey?>(null)
    override val savedKey: StateFlow<SavedKey?> = _savedKey.asStateFlow()

    private val _savedKeys = MutableStateFlow<Map<CloudProvider, SavedKey>>(emptyMap())
    override val savedKeys: StateFlow<Map<CloudProvider, SavedKey>> = _savedKeys.asStateFlow()

    init {
        reload()
    }

    override suspend fun complete(system: String, user: String): Result<String, AssistantError> {
        val credentials = activeCredentials() ?: return Result.Failure(AssistantError.NO_ENGINE)
        return remote.complete(credentials, system, user)
    }

    // A key is saved only after the provider accepted it, so a typo never replaces a working key.
    override suspend fun saveKey(credentials: CloudCredentials): EmptyResult<AssistantError> {
        val trimmed = credentials.copy(apiKey = credentials.apiKey.trim(), model = credentials.model.trim())
        return remote.verify(trimmed).onSuccess {
            keys.set(trimmed.provider, trimmed.apiKey)
            preferences.setModel(trimmed.provider, trimmed.model)
            preferences.setProvider(trimmed.provider)
            reload()
        }
    }

    override fun activate(provider: CloudProvider) {
        if (keys.get(provider) == null) return
        preferences.setProvider(provider)
        reload()
    }

    override fun removeKey() {
        preferences.provider()?.let(keys::remove)
        preferences.setProvider(null)
        reload()
    }

    private fun activeCredentials(): CloudCredentials? {
        val provider = preferences.provider() ?: return null
        val key = keys.get(provider) ?: return null
        return CloudCredentials(provider, key, preferences.model(provider) ?: provider.defaultModel)
    }

    private fun reload() {
        val credentials = activeCredentials()
        _status.value = credentials?.let { AssistantStatus.Ready(AssistantEngine.CLOUD, it.provider) } ?: AssistantStatus.NeedsSetup
        _savedKey.value = credentials?.let { SavedKey(it.provider, it.apiKey.takeLast(4), it.model) }
        _savedKeys.value = CloudProvider.entries.mapNotNull { provider ->
            keys.get(provider)?.let { key -> provider to SavedKey(provider, key.takeLast(4), preferences.model(provider) ?: provider.defaultModel) }
        }.toMap()
    }
}

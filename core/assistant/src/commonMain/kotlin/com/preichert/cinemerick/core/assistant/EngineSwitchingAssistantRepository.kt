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
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiDataSource
import com.preichert.cinemerick.core.domain.assistant.SystemAiDownload
import com.preichert.cinemerick.core.domain.assistant.SystemAiState
import com.preichert.cinemerick.core.domain.onSuccess
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val log = Logger.withTag("AssistantRepository")

/**
 * The assistant answered by the AI of the system or by a cloud provider with the user's own key.
 *
 * Which one answers: the engine the user chose; before any choice, the system AI when it is ready (nothing leaves the
 * device), otherwise the cloud provider that has a key. A chosen engine that is not ready is never replaced by the other
 * one: the request would silently go (or stop going) to the cloud.
 *
 * The active provider is in the preferences and its key in the secure key store: both are read again whenever a key
 * changes. The system AI is asked again when [refreshSystemAi] is called.
 */
class EngineSwitchingAssistantRepository(
    private val preferences: AssistantPreferences,
    private val keys: ApiKeyLocalDataSource,
    private val remote: CloudLlmRemoteDataSource,
    private val systemAiSource: SystemAiDataSource,
    // The download goes on after the screen that started it is closed.
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AssistantRepository {

    private val _status = MutableStateFlow<AssistantStatus>(AssistantStatus.NeedsSetup)
    override val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _savedKey = MutableStateFlow<SavedKey?>(null)
    override val savedKey: StateFlow<SavedKey?> = _savedKey.asStateFlow()

    private val _savedKeys = MutableStateFlow<Map<CloudProvider, SavedKey>>(emptyMap())
    override val savedKeys: StateFlow<Map<CloudProvider, SavedKey>> = _savedKeys.asStateFlow()

    private val _systemAi = MutableStateFlow(SystemAiState(name = systemAiSource.name))
    override val systemAi: StateFlow<SystemAiState> = _systemAi.asStateFlow()

    private val _chosenEngine = MutableStateFlow<AssistantEngine?>(null)
    override val chosenEngine: StateFlow<AssistantEngine?> = _chosenEngine.asStateFlow()

    private var downloadJob: Job? = null

    init {
        reload()
    }

    override suspend fun complete(system: String, user: String): Result<String, AssistantError> =
        when (resolveEngine()) {
            AssistantEngine.SYSTEM -> systemAiSource.complete(system, user)
            AssistantEngine.CLOUD -> activeCredentials()?.let { remote.complete(it, system, user) } ?: Result.Failure(AssistantError.NO_ENGINE)
            null -> Result.Failure(AssistantError.NO_ENGINE)
        }

    override suspend fun refreshSystemAi() {
        // A download in flight reports by itself: asking in the middle would show it as not started.
        if (downloadJob?.isActive == true) return
        val availability = systemAiSource.availability()
        log.i { "System AI (${systemAiSource.name}): $availability" }
        _systemAi.update { it.copy(availability = availability, downloadedBytes = null) }
        reload()
    }

    override fun downloadSystemAi() {
        if (_systemAi.value.availability != SystemAiAvailability.DOWNLOADABLE || downloadJob?.isActive == true) return
        // Asking for the download is asking to use it.
        preferences.setEngine(AssistantEngine.SYSTEM)
        _systemAi.update { it.copy(availability = SystemAiAvailability.DOWNLOADING, downloadedBytes = 0L) }
        reload()
        downloadJob = scope.launch {
            systemAiSource.download().collect { event ->
                when (event) {
                    is SystemAiDownload.Progress -> _systemAi.update { it.copy(downloadedBytes = event.bytes) }
                    SystemAiDownload.Completed, SystemAiDownload.Failed -> Unit
                }
            }
        }.also { job ->
            job.invokeOnCompletion {
                // Whatever ended it, what the system says now is the truth: ready, or still to download.
                scope.launch {
                    val availability = systemAiSource.availability()
                    _systemAi.update { it.copy(availability = availability, downloadedBytes = null) }
                    reload()
                }
            }
        }
    }

    override fun selectEngine(engine: AssistantEngine) {
        preferences.setEngine(engine)
        reload()
    }

    // A key is saved only after the provider accepted it, so a typo never replaces a working key.
    override suspend fun saveKey(credentials: CloudCredentials): EmptyResult<AssistantError> {
        val trimmed = credentials.copy(apiKey = credentials.apiKey.trim(), model = credentials.model.trim())
        return remote.verify(trimmed).onSuccess {
            keys.set(trimmed.provider, trimmed.apiKey)
            preferences.setModel(trimmed.provider, trimmed.model)
            preferences.setProvider(trimmed.provider)
            // Saving a key is asking to use it, also where the system AI would have been the default.
            preferences.setEngine(AssistantEngine.CLOUD)
            reload()
        }
    }

    override fun activate(provider: CloudProvider) {
        if (keys.get(provider) == null) return
        preferences.setProvider(provider)
        preferences.setEngine(AssistantEngine.CLOUD)
        reload()
    }

    override fun removeKey() {
        preferences.provider()?.let(keys::remove)
        preferences.setProvider(null)
        // Without its key the cloud choice is void: the default (the system AI, if ready) applies again.
        if (preferences.engine() == AssistantEngine.CLOUD) preferences.setEngine(null)
        reload()
    }

    private fun activeCredentials(): CloudCredentials? {
        val provider = preferences.provider() ?: return null
        val key = keys.get(provider) ?: return null
        return CloudCredentials(provider, key, preferences.model(provider) ?: provider.defaultModel)
    }

    private fun resolveEngine(): AssistantEngine? {
        val systemReady = _systemAi.value.availability == SystemAiAvailability.AVAILABLE
        val cloudReady = activeCredentials() != null
        return when (preferences.engine()) {
            AssistantEngine.SYSTEM -> AssistantEngine.SYSTEM.takeIf { systemReady }
            AssistantEngine.CLOUD -> AssistantEngine.CLOUD.takeIf { cloudReady }
            null -> when {
                systemReady -> AssistantEngine.SYSTEM
                cloudReady -> AssistantEngine.CLOUD
                else -> null
            }
        }
    }

    private fun reload() {
        val credentials = activeCredentials()
        _status.value = when (resolveEngine()) {
            AssistantEngine.SYSTEM -> AssistantStatus.Ready(AssistantEngine.SYSTEM)
            AssistantEngine.CLOUD -> AssistantStatus.Ready(AssistantEngine.CLOUD, credentials?.provider)
            null -> AssistantStatus.NeedsSetup
        }
        _chosenEngine.value = preferences.engine()
        _savedKey.value = credentials?.let { SavedKey(it.provider, it.apiKey.takeLast(4), it.model) }
        _savedKeys.value = CloudProvider.entries.mapNotNull { provider ->
            keys.get(provider)?.let { key -> provider to SavedKey(provider, key.takeLast(4), preferences.model(provider) ?: provider.defaultModel) }
        }.toMap()
    }
}

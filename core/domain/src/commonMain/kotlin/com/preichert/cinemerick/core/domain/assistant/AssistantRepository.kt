package com.preichert.cinemerick.core.domain.assistant

import com.preichert.cinemerick.core.domain.EmptyResult
import com.preichert.cinemerick.core.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The assistant the app talks to: which engine is set up, and one-shot completions with it. Combines the saved
 * settings, the key store and the engines, so the features never see a key.
 */
interface AssistantRepository {
    val status: StateFlow<AssistantStatus>

    /** The key saved for the active provider, or null when there is none. */
    val savedKey: StateFlow<SavedKey?>

    /** Every provider with a key saved on this device, active or not: switching to one needs no new key. */
    val savedKeys: StateFlow<Map<CloudProvider, SavedKey>>

    /** The AI the system offers on this device, and whether it is ready. */
    val systemAi: StateFlow<SystemAiState>

    /** The engine the user chose, or null before any choice: then the system AI is used where it is available. */
    val chosenEngine: StateFlow<AssistantEngine?>

    /** Asks the system again whether its AI is ready: the user may have turned it on in the settings meanwhile. */
    suspend fun refreshSystemAi()

    /** Starts the download of the system AI. It goes on while the screen that asked for it is closed. */
    fun downloadSystemAi()

    /** Makes [engine] the one that answers. A system engine that is not ready yet is kept until it is. */
    fun selectEngine(engine: AssistantEngine)

    /** Asks the active engine for one answer to [user], following [system]. */
    suspend fun complete(system: String, user: String): Result<String, AssistantError>

    /** Checks [credentials] with the provider and, when they work, saves them and makes them the active engine. */
    suspend fun saveKey(credentials: CloudCredentials): EmptyResult<AssistantError>

    /** Makes [provider] the active engine with the key already saved for it. Does nothing when there is none. */
    fun activate(provider: CloudProvider)

    /** Removes the active provider's key; the other saved keys stay. */
    fun removeKey()
}

/** The AI the operating system provides, run on the device. */
interface SystemAiDataSource {
    /** How the AI is called, or null on a platform that has none. */
    val name: String?

    suspend fun availability(): SystemAiAvailability

    /** Downloads the AI; only [SystemAiAvailability.DOWNLOADABLE] ones can be. */
    fun download(): Flow<SystemAiDownload>

    /** Asks for one answer to [user], following [system]. */
    suspend fun complete(system: String, user: String): Result<String, AssistantError>
}

/** One-shot calls to a provider's API. */
interface CloudLlmRemoteDataSource {
    suspend fun complete(credentials: CloudCredentials, system: String, user: String): Result<String, AssistantError>

    /** A call that only succeeds with a valid key. */
    suspend fun verify(credentials: CloudCredentials): EmptyResult<AssistantError>
}

/** The provider keys, kept in the platform's secure storage. */
interface ApiKeyLocalDataSource {
    fun get(provider: CloudProvider): String?
    fun set(provider: CloudProvider, key: String)
    fun remove(provider: CloudProvider)
}

/** Assistant choices that are not secret. */
interface AssistantPreferences {
    /** The engine the user chose, or null if they never did. */
    fun engine(): AssistantEngine?
    fun setEngine(engine: AssistantEngine?)

    fun provider(): CloudProvider?
    fun setProvider(provider: CloudProvider?)
    fun model(provider: CloudProvider): String?
    fun setModel(provider: CloudProvider, model: String)
}

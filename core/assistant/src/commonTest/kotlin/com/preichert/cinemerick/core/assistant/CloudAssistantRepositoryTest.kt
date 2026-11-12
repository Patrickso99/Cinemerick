package com.preichert.cinemerick.core.assistant

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.preichert.cinemerick.core.domain.EmptyResult
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.ApiKeyLocalDataSource
import com.preichert.cinemerick.core.domain.assistant.AssistantEngine
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.AssistantStatus
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudLlmRemoteDataSource
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CloudAssistantRepositoryTest {

    private class FakeKeys : ApiKeyLocalDataSource {
        val keys = mutableMapOf<CloudProvider, String>()
        override fun get(provider: CloudProvider) = keys[provider]
        override fun set(provider: CloudProvider, key: String) { keys[provider] = key }
        override fun remove(provider: CloudProvider) { keys.remove(provider) }
    }

    private class FakeRemote : CloudLlmRemoteDataSource {
        var verifyResult: EmptyResult<AssistantError> = Result.Success(Unit)
        var lastCompleted: CloudCredentials? = null
        override suspend fun complete(credentials: CloudCredentials, system: String, user: String): Result<String, AssistantError> {
            lastCompleted = credentials
            return Result.Success("{}")
        }
        override suspend fun verify(credentials: CloudCredentials): EmptyResult<AssistantError> = verifyResult
    }

    private val keys = FakeKeys()
    private val remote = FakeRemote()
    private val preferences = SettingsAssistantPreferences(MemorySettings())

    private fun repository() = CloudAssistantRepository(preferences, keys, remote)

    @Test
    fun withoutAKeyTheAssistantNeedsSetupAndAnswersNoEngine() = runTest {
        val repository = repository()

        assertThat(repository.status.value).isEqualTo(AssistantStatus.NeedsSetup)
        assertThat(repository.complete("s", "u")).isEqualTo(Result.Failure(AssistantError.NO_ENGINE))
    }

    @Test
    fun acceptedKeyIsSavedTrimmedAndBecomesTheActiveEngine() = runTest {
        val repository = repository()

        repository.saveKey(CloudCredentials(CloudProvider.ANTHROPIC, "  sk-ant-abc123\n", " claude-opus-5-5 "))

        assertThat(keys.keys[CloudProvider.ANTHROPIC]).isEqualTo("sk-ant-abc123")
        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.ANTHROPIC))
        assertThat(repository.savedKey.value).isEqualTo(SavedKey(CloudProvider.ANTHROPIC, "c123", "claude-opus-5-5"))
    }

    @Test
    fun rejectedKeyNeverReplacesTheWorkingOne() = runTest {
        val repository = repository()
        repository.saveKey(CloudCredentials(CloudProvider.OPENAI, "sk-good", "gpt-5-mini"))

        remote.verifyResult = Result.Failure(AssistantError.INVALID_KEY)
        val result = repository.saveKey(CloudCredentials(CloudProvider.ANTHROPIC, "sk-ant-typo", "claude-opus-5-5"))

        assertThat(result).isEqualTo(Result.Failure(AssistantError.INVALID_KEY))
        assertThat(keys.keys[CloudProvider.ANTHROPIC]).isNull()
        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.OPENAI))
    }

    @Test
    fun completionUsesTheSavedModelOrTheProviderDefault() = runTest {
        keys.set(CloudProvider.MISTRAL, "key")
        preferences.setProvider(CloudProvider.MISTRAL)

        repository().complete("s", "u")

        assertThat(remote.lastCompleted?.model).isEqualTo(CloudProvider.MISTRAL.defaultModel)
    }

    @Test
    fun removingTheKeyNeedsSetupAgain() = runTest {
        val repository = repository()
        repository.saveKey(CloudCredentials(CloudProvider.GEMINI, "AIzaXYZ", "gemini-2.5-flash"))

        repository.removeKey()

        assertThat(keys.keys[CloudProvider.GEMINI]).isNull()
        assertThat(repository.status.value).isEqualTo(AssistantStatus.NeedsSetup)
        assertThat(repository.savedKey.value).isNull()
    }

    @Test
    fun providerSavedWithoutKeyStillNeedsSetup() = runTest {
        // The key store lost the key (e.g. restored on another device) while the preference survived.
        preferences.setProvider(CloudProvider.OPENROUTER)

        assertThat(repository().status.value).isEqualTo(AssistantStatus.NeedsSetup)
    }

    @Test
    fun keyShorterThanFourCharactersIsShownWhole() = runTest {
        val repository = repository()
        repository.saveKey(CloudCredentials(CloudProvider.GROK, "ab", "grok-3-mini"))

        assertThat(repository.savedKey.value?.lastFour).isEqualTo("ab")
    }

    @Test
    fun everySavedKeyIsListedAndAnotherCanBeActivatedWithoutANewKey() = runTest {
        val repository = repository()
        repository.saveKey(CloudCredentials(CloudProvider.GEMINI, "AIzaGemini", "gemini-2.5-flash"))
        repository.saveKey(CloudCredentials(CloudProvider.OPENAI, "sk-openai", "gpt-5-mini"))

        assertThat(repository.savedKeys.value.keys).isEqualTo(setOf(CloudProvider.GEMINI, CloudProvider.OPENAI))

        repository.activate(CloudProvider.GEMINI)

        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.GEMINI))
        assertThat(repository.savedKey.value).isEqualTo(SavedKey(CloudProvider.GEMINI, "mini", "gemini-2.5-flash"))
    }

    @Test
    fun activatingAProviderWithoutAKeyChangesNothing() = runTest {
        val repository = repository()
        repository.saveKey(CloudCredentials(CloudProvider.OPENAI, "sk-openai", "gpt-5-mini"))

        repository.activate(CloudProvider.MISTRAL)

        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.OPENAI))
    }

    @Test
    fun removingTheActiveKeyKeepsTheOthers() = runTest {
        val repository = repository()
        repository.saveKey(CloudCredentials(CloudProvider.GEMINI, "AIzaGemini", "gemini-2.5-flash"))
        repository.saveKey(CloudCredentials(CloudProvider.OPENAI, "sk-openai", "gpt-5-mini"))

        repository.removeKey()

        assertThat(repository.savedKeys.value.keys).isEqualTo(setOf(CloudProvider.GEMINI))
        assertThat(repository.status.value).isEqualTo(AssistantStatus.NeedsSetup)
    }
}

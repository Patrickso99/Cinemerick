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
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiDownload
import com.preichert.cinemerick.core.testing.FakeSystemAiDataSource
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EngineSwitchingAssistantRepositoryTest {

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
    private val systemAi = FakeSystemAiDataSource()

    private fun repository(scope: CoroutineScope = CoroutineScope(Job())) =
        EngineSwitchingAssistantRepository(preferences, keys, remote, systemAi, scope)

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

    // ---------- The system AI ----------

    @Test
    fun withoutAChoiceTheSystemAiAnswersWhenItIsReady() = runTest {
        systemAi.availability = SystemAiAvailability.AVAILABLE
        systemAi.answer = Result.Success("""{"films":["Hope"]}""")
        val repository = repository()

        repository.refreshSystemAi()

        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.SYSTEM))
        assertThat(repository.complete("s", "u")).isEqualTo(Result.Success("""{"films":["Hope"]}"""))
        assertThat(systemAi.lastUser).isEqualTo("u")
        assertThat(remote.lastCompleted).isNull()
    }

    @Test
    fun theSystemAiBeatsACloudKeyUntilTheUserChoosesTheCloud() = runTest {
        keys.set(CloudProvider.OPENAI, "sk-openai")
        preferences.setProvider(CloudProvider.OPENAI)
        systemAi.availability = SystemAiAvailability.AVAILABLE
        val repository = repository()
        repository.refreshSystemAi()
        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.SYSTEM))

        repository.selectEngine(AssistantEngine.CLOUD)

        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.OPENAI))
        repository.complete("s", "u")
        assertThat(remote.lastCompleted?.provider).isEqualTo(CloudProvider.OPENAI)
    }

    @Test
    fun savingAKeyChoosesTheCloudEvenWhereTheSystemAiIsReady() = runTest {
        systemAi.availability = SystemAiAvailability.AVAILABLE
        val repository = repository()
        repository.refreshSystemAi()

        repository.saveKey(CloudCredentials(CloudProvider.GEMINI, "AIzaKey", "gemini-2.5-flash"))

        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.GEMINI))
        assertThat(repository.chosenEngine.value).isEqualTo(AssistantEngine.CLOUD)
    }

    @Test
    fun aChosenSystemAiThatIsNotReadyIsNeverReplacedByTheCloud() = runTest {
        keys.set(CloudProvider.OPENAI, "sk-openai")
        preferences.setProvider(CloudProvider.OPENAI)
        preferences.setEngine(AssistantEngine.SYSTEM)
        systemAi.availability = SystemAiAvailability.NEEDS_ENABLING
        val repository = repository()
        repository.refreshSystemAi()

        assertThat(repository.status.value).isEqualTo(AssistantStatus.NeedsSetup)
        assertThat(repository.complete("s", "u")).isEqualTo(Result.Failure(AssistantError.NO_ENGINE))
        assertThat(remote.lastCompleted).isNull()
    }

    @Test
    fun withoutAChoiceAndWithoutTheSystemAiTheCloudKeyAnswers() = runTest {
        keys.set(CloudProvider.MISTRAL, "key")
        preferences.setProvider(CloudProvider.MISTRAL)
        systemAi.availability = SystemAiAvailability.UNSUPPORTED
        val repository = repository()
        repository.refreshSystemAi()

        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.CLOUD, CloudProvider.MISTRAL))
    }

    @Test
    fun removingTheKeyGivesTheSystemAiBackAsTheDefault() = runTest {
        systemAi.availability = SystemAiAvailability.AVAILABLE
        val repository = repository()
        repository.refreshSystemAi()
        repository.saveKey(CloudCredentials(CloudProvider.GEMINI, "AIzaKey", "gemini-2.5-flash"))

        repository.removeKey()

        assertThat(repository.chosenEngine.value).isNull()
        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.SYSTEM))
    }

    @Test
    fun theSystemAiThatTurnsOffMidwayIsNoEngineNotACrash() = runTest {
        systemAi.availability = SystemAiAvailability.AVAILABLE
        val repository = repository()
        repository.refreshSystemAi()

        systemAi.availability = SystemAiAvailability.NEEDS_ENABLING
        repository.refreshSystemAi()

        assertThat(repository.status.value).isEqualTo(AssistantStatus.NeedsSetup)
        assertThat(repository.systemAi.value.availability).isEqualTo(SystemAiAvailability.NEEDS_ENABLING)
    }

    @Test
    fun aPlatformWithoutASystemAiHasNoNameForIt() = runTest {
        val repository = EngineSwitchingAssistantRepository(preferences, keys, remote, FakeSystemAiDataSource(name = null))

        assertThat(repository.systemAi.value.name).isNull()
    }

    @Test
    fun downloadingTheSystemAiShowsItsProgressThenUsesIt() = runTest(UnconfinedTestDispatcher()) {
        systemAi.availability = SystemAiAvailability.DOWNLOADABLE
        val repository = repository(backgroundScope)
        repository.refreshSystemAi()
        assertThat(repository.status.value).isEqualTo(AssistantStatus.NeedsSetup)

        repository.downloadSystemAi()
        assertThat(repository.systemAi.value.availability).isEqualTo(SystemAiAvailability.DOWNLOADING)
        systemAi.downloadEvents.emit(SystemAiDownload.Progress(1_500L))
        assertThat(repository.systemAi.value.downloadedBytes).isEqualTo(1_500L)

        systemAi.downloadEvents.emit(SystemAiDownload.Completed)

        assertThat(repository.systemAi.value.availability).isEqualTo(SystemAiAvailability.AVAILABLE)
        assertThat(repository.systemAi.value.downloadedBytes).isNull()
        assertThat(repository.status.value).isEqualTo(AssistantStatus.Ready(AssistantEngine.SYSTEM))
        assertThat(repository.chosenEngine.value).isEqualTo(AssistantEngine.SYSTEM)
    }

    @Test
    fun aFailedDownloadCanBeStartedAgain() = runTest(UnconfinedTestDispatcher()) {
        systemAi.availability = SystemAiAvailability.DOWNLOADABLE
        val repository = repository(backgroundScope)
        repository.refreshSystemAi()
        repository.downloadSystemAi()

        systemAi.downloadEvents.emit(SystemAiDownload.Failed)
        assertThat(repository.systemAi.value.availability).isEqualTo(SystemAiAvailability.DOWNLOADABLE)

        repository.downloadSystemAi()
        assertThat(systemAi.downloads).isEqualTo(2)
    }

    @Test
    fun theDownloadStartsOnceEvenIfAskedTwiceAndOnlyWhenDownloadable() = runTest(UnconfinedTestDispatcher()) {
        systemAi.availability = SystemAiAvailability.UNSUPPORTED
        val repository = repository(backgroundScope)
        repository.refreshSystemAi()
        repository.downloadSystemAi()
        assertThat(systemAi.downloads).isEqualTo(0)

        systemAi.availability = SystemAiAvailability.DOWNLOADABLE
        repository.refreshSystemAi()
        repository.downloadSystemAi()
        repository.downloadSystemAi()

        assertThat(systemAi.downloads).isEqualTo(1)
    }

    @Test
    fun askingAgainDuringTheDownloadDoesNotShowItAsNotStarted() = runTest(UnconfinedTestDispatcher()) {
        systemAi.availability = SystemAiAvailability.DOWNLOADABLE
        val repository = repository(backgroundScope)
        repository.refreshSystemAi()
        repository.downloadSystemAi()

        repository.refreshSystemAi()

        assertThat(repository.systemAi.value.availability).isEqualTo(SystemAiAvailability.DOWNLOADING)
    }

    @Test
    fun theChosenEngineSurvivesARestartAndAnUnknownNameIsNoChoice() = runTest {
        val settings = MemorySettings()
        SettingsAssistantPreferences(settings).setEngine(AssistantEngine.SYSTEM)
        assertThat(SettingsAssistantPreferences(settings).engine()).isEqualTo(AssistantEngine.SYSTEM)

        settings.putString("assistant_engine", "GEMMA")
        assertThat(SettingsAssistantPreferences(settings).engine()).isNull()

        SettingsAssistantPreferences(settings).setEngine(null)
        assertThat(settings.hasKey("assistant_engine")).isEqualTo(false)
    }
}

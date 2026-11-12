package com.preichert.cinemerick.feature.settings.presentation.key

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.testing.FakeAssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AssistantKeyViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsOnTheConnectedProviderWithItsModel() = runTest {
        val assistant = FakeAssistantRepository().apply { connect(CloudProvider.GEMINI) }

        val state = AssistantKeyViewModel(assistant).state.value

        assertThat(state.provider).isEqualTo(CloudProvider.GEMINI)
        assertThat(state.model).isEqualTo(CloudProvider.GEMINI.defaultModel)
        assertThat(state.key).isEqualTo("")
    }

    @Test
    fun switchingProviderSuggestsItsModelAndClearsTheError() = runTest {
        val viewModel = AssistantKeyViewModel(FakeAssistantRepository())
        viewModel.onAction(AssistantKeyAction.OnSaveClick)
        assertThat(viewModel.state.value.error).isEqualTo(KeyErrorUi.EMPTY)

        viewModel.onAction(AssistantKeyAction.OnProviderSelect(CloudProvider.MISTRAL))

        assertThat(viewModel.state.value.model).isEqualTo("mistral-small-latest")
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun blankKeyIsNotSentToTheProvider() = runTest {
        val assistant = FakeAssistantRepository()
        val viewModel = AssistantKeyViewModel(assistant)
        viewModel.onAction(AssistantKeyAction.OnKeyChange(" \t "))

        viewModel.onAction(AssistantKeyAction.OnSaveClick)

        assertThat(viewModel.state.value.error).isEqualTo(KeyErrorUi.EMPTY)
        assertThat(assistant.savedCredentials.size).isEqualTo(0)
    }

    @Test
    fun acceptedKeyNavigatesBack() = runTest {
        val assistant = FakeAssistantRepository()
        val viewModel = AssistantKeyViewModel(assistant)
        viewModel.onAction(AssistantKeyAction.OnProviderSelect(CloudProvider.ANTHROPIC))
        viewModel.onAction(AssistantKeyAction.OnKeyChange("sk-ant-123"))
        viewModel.onAction(AssistantKeyAction.OnModelChange("  "))

        viewModel.events.test {
            viewModel.onAction(AssistantKeyAction.OnSaveClick)
            assertThat(awaitItem()).isEqualTo(AssistantKeyEvent.Saved)
        }
        // A blank model falls back to the provider's own.
        assertThat(assistant.savedCredentials.single()).isEqualTo(CloudCredentials(CloudProvider.ANTHROPIC, "sk-ant-123", CloudProvider.ANTHROPIC.defaultModel))
        assertThat(viewModel.state.value.isVerifying).isEqualTo(false)
    }

    @Test
    fun rejectedKeyIsShownWithTheError() = runTest {
        val assistant = FakeAssistantRepository(saveResult = Result.Failure(AssistantError.INVALID_KEY))
        val viewModel = AssistantKeyViewModel(assistant)
        viewModel.onAction(AssistantKeyAction.OnKeyChange("sk-typo"))

        viewModel.onAction(AssistantKeyAction.OnSaveClick)

        assertThat(viewModel.state.value.error).isEqualTo(KeyErrorUi.INVALID)
        assertThat(viewModel.state.value.isKeyVisible).isEqualTo(true)
        assertThat(viewModel.state.value.isVerifying).isEqualTo(false)
    }

    @Test
    fun offlineAndRateLimitHaveTheirOwnMessage() = runTest {
        val assistant = FakeAssistantRepository(saveResult = Result.Failure(AssistantError.OFFLINE))
        val viewModel = AssistantKeyViewModel(assistant)
        viewModel.onAction(AssistantKeyAction.OnKeyChange("k"))
        viewModel.onAction(AssistantKeyAction.OnSaveClick)
        assertThat(viewModel.state.value.error).isEqualTo(KeyErrorUi.OFFLINE)
        assertThat(viewModel.state.value.isKeyVisible).isEqualTo(false)

        assistant.saveResult = Result.Failure(AssistantError.RATE_LIMITED)
        viewModel.onAction(AssistantKeyAction.OnSaveClick)
        assertThat(viewModel.state.value.error).isEqualTo(KeyErrorUi.RATE_LIMITED)
    }

    @Test
    fun editingTheKeyClearsTheError() = runTest {
        val viewModel = AssistantKeyViewModel(FakeAssistantRepository(saveResult = Result.Failure(AssistantError.INVALID_KEY)))
        viewModel.onAction(AssistantKeyAction.OnKeyChange("bad"))
        viewModel.onAction(AssistantKeyAction.OnSaveClick)

        viewModel.onAction(AssistantKeyAction.OnKeyChange("bad2"))

        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun savedKeyOfAnotherProviderIsActivatedWithoutANewKey() = runTest {
        val assistant = FakeAssistantRepository().apply {
            connect(CloudProvider.GEMINI, "AIzaGemini")
            connect(CloudProvider.OPENAI, "sk-openai")
        }
        val viewModel = AssistantKeyViewModel(assistant)
        assertThat(viewModel.state.value.activeProvider).isEqualTo(CloudProvider.OPENAI)

        viewModel.onAction(AssistantKeyAction.OnProviderSelect(CloudProvider.GEMINI))
        assertThat(viewModel.state.value.savedKey?.lastFour).isEqualTo("mini")

        viewModel.events.test {
            viewModel.onAction(AssistantKeyAction.OnUseSavedKeyClick)
            assertThat(awaitItem()).isEqualTo(AssistantKeyEvent.Saved)
        }
        assertThat(assistant.savedKey.value?.provider).isEqualTo(CloudProvider.GEMINI)
        assertThat(assistant.savedCredentials.size).isEqualTo(0)
    }

    @Test
    fun useSavedKeyWithoutOneDoesNothing() = runTest {
        val assistant = FakeAssistantRepository()
        val viewModel = AssistantKeyViewModel(assistant)

        viewModel.onAction(AssistantKeyAction.OnUseSavedKeyClick)

        assertThat(assistant.savedKey.value).isNull()
    }
}

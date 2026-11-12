package com.preichert.cinemerick.feature.settings.presentation.key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.onFailure
import com.preichert.cinemerick.core.domain.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Connecting a provider: the key is checked with the provider before it is saved, so a typo never replaces a key
 * that works. Starts on the provider already connected, if any.
 */
class AssistantKeyViewModel(
    private val assistant: AssistantRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(assistant.savedKeys, assistant.savedKey) { keys, active -> keys to active?.provider }.collect { (keys, active) ->
                _state.update { it.copy(savedKeys = keys, activeProvider = active) }
            }
        }
    }

    // Only the view model knows when the provider accepted the key.
    private val _events = Channel<AssistantKeyEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: AssistantKeyAction) {
        when (action) {
            is AssistantKeyAction.OnProviderSelect -> _state.update {
                it.copy(provider = action.provider, model = modelFor(action.provider), error = null)
            }
            is AssistantKeyAction.OnKeyChange -> _state.update { it.copy(key = action.key, error = null) }
            is AssistantKeyAction.OnModelChange -> _state.update { it.copy(model = action.model) }
            AssistantKeyAction.OnKeyVisibilityToggle -> _state.update { it.copy(isKeyVisible = !it.isKeyVisible) }
            AssistantKeyAction.OnSaveClick -> save()
            AssistantKeyAction.OnUseSavedKeyClick -> useSavedKey()
        }
    }

    private fun save() {
        val current = _state.value
        if (current.isVerifying) return
        if (current.key.isBlank()) {
            _state.update { it.copy(error = KeyErrorUi.EMPTY) }
            return
        }
        val model = current.model.ifBlank { current.provider.defaultModel }
        _state.update { it.copy(isVerifying = true, error = null) }
        viewModelScope.launch {
            assistant.saveKey(CloudCredentials(current.provider, current.key, model))
                .onSuccess {
                    _state.update { it.copy(isVerifying = false) }
                    _events.send(AssistantKeyEvent.Saved)
                }
                .onFailure { error ->
                    // An invalid key is shown so the user can check it.
                    _state.update {
                        it.copy(isVerifying = false, error = error.toKeyError(), isKeyVisible = it.isKeyVisible || error == AssistantError.INVALID_KEY)
                    }
                }
        }
    }

    // The key was checked when it was saved: switching to it needs no new check.
    private fun useSavedKey() {
        val provider = _state.value.provider
        if (_state.value.savedKeys[provider] == null) return
        assistant.activate(provider)
        _events.trySend(AssistantKeyEvent.Saved)
    }

    private fun modelFor(provider: CloudProvider): String = assistant.savedKeys.value[provider]?.model ?: provider.defaultModel

    private fun initialState(): AssistantKeyState {
        val provider = assistant.savedKey.value?.provider ?: CloudProvider.OPENAI
        return AssistantKeyState(
            provider = provider,
            model = modelFor(provider),
            savedKeys = assistant.savedKeys.value,
            activeProvider = assistant.savedKey.value?.provider,
        )
    }
}

sealed interface AssistantKeyEvent {
    data object Saved : AssistantKeyEvent
}

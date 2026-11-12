package com.preichert.cinemerick.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.presentation.LanguageController
import com.preichert.cinemerick.core.presentation.AnimationController
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.presentation.TimeWindow
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.domain.assistant.AssistantEngine
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.core.domain.assistant.AssistantStatus
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.core.domain.assistant.SystemAiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeController: ThemeController,
    private val animationController: AnimationController,
    private val timeWindowController: TimeWindowController,
    private val languageController: LanguageController,
    private val assistant: AssistantRepository,
) : ViewModel() {

    private val isRemoveKeyDialogOpen = MutableStateFlow(false)

    private val settings = combine(
        themeController.mode,
        animationController.isEnabled,
        timeWindowController.window,
        languageController.language,
        ::toState
    )

    // What the assistant section shows, from the four things that decide it.
    private val assistantParts = combine(assistant.savedKey, assistant.systemAi, assistant.status, assistant.chosenEngine) { key, systemAi, status, chosen ->
        AssistantParts(key, systemAi, (status as? AssistantStatus.Ready)?.engine, chosen)
    }

    val state: StateFlow<SettingsState> = combine(settings, assistantParts, isRemoveKeyDialogOpen) { state, parts, dialogOpen ->
        state.withAssistant(parts).copy(isRemoveKeyDialogOpen = dialogOpen && parts.key != null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = toState(
            themeController.mode.value,
            animationController.isEnabled.value,
            timeWindowController.window.value,
            languageController.language.value,
        ).withAssistant(
            AssistantParts(
                assistant.savedKey.value,
                assistant.systemAi.value,
                (assistant.status.value as? AssistantStatus.Ready)?.engine,
                assistant.chosenEngine.value,
            )
        )
    )

    init {
        // The system AI may have been turned on or off in the system settings since the last time.
        viewModelScope.launch { assistant.refreshSystemAi() }
    }

    private class AssistantParts(val key: SavedKey?, val systemAi: SystemAiState, val active: AssistantEngine?, val chosen: AssistantEngine?)

    private fun SettingsState.withAssistant(parts: AssistantParts) =
        copy(assistantKey = parts.key, systemAi = parts.systemAi, activeEngine = parts.active, chosenEngine = parts.chosen)

    private fun toState(themeMode: ThemeMode, animationsEnabled: Boolean, window: TimeWindow, language: AppLanguage) = SettingsState(
        themeMode = themeMode,
        language = language,
        animationsEnabled = animationsEnabled,
        defaultMinMinutes = window.minMinutes,
        defaultMaxMinutes = window.maxMinutes,
    )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnLanguageSelected -> languageController.setLanguage(action.language)
            is SettingsAction.OnAutoToggled -> {
                val newMode = if (action.enabled) {
                    ThemeMode.SYSTEM
                } else {
                    if (action.systemDark) ThemeMode.DARK else ThemeMode.LIGHT
                }
                themeController.setMode(newMode)
            }
            is SettingsAction.OnThemeModeSelected -> {
                themeController.setMode(action.mode)
            }
            is SettingsAction.OnAnimationsToggled -> {
                animationController.setEnabled(action.enabled)
            }
            // The dialog blocks invalid windows; the controller rejects them as a safety net.
            is SettingsAction.OnDefaultMinTimeChanged -> timeWindowController.setMin(action.minutes)
            is SettingsAction.OnDefaultMaxTimeChanged -> timeWindowController.setMax(action.minutes)
            is SettingsAction.OnEngineSelect -> assistant.selectEngine(action.engine)
            SettingsAction.OnSystemAiDownload -> assistant.downloadSystemAi()
            SettingsAction.OnRemoveKeyClick -> isRemoveKeyDialogOpen.value = true
            SettingsAction.OnRemoveKeyDismiss -> isRemoveKeyDialogOpen.value = false
            SettingsAction.OnRemoveKeyConfirm -> {
                assistant.removeKey()
                isRemoveKeyDialogOpen.value = false
            }
        }
    }
}
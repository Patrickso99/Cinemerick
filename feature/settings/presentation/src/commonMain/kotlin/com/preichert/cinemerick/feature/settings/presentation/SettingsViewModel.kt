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
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val themeController: ThemeController,
    private val animationController: AnimationController,
    private val timeWindowController: TimeWindowController,
    private val languageController: LanguageController,
    private val assistant: AssistantRepository,
) : ViewModel() {

    private val isRemoveKeyDialogOpen = MutableStateFlow(false)

    val state: StateFlow<SettingsState> = combine(
        combine(themeController.mode, animationController.isEnabled, timeWindowController.window, languageController.language, ::toState),
        assistant.savedKey,
        isRemoveKeyDialogOpen,
    ) { state, key, dialogOpen ->
        state.copy(assistantKey = key, isRemoveKeyDialogOpen = dialogOpen && key != null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = toState(
            themeController.mode.value,
            animationController.isEnabled.value,
            timeWindowController.window.value,
            languageController.language.value,
        ).copy(assistantKey = assistant.savedKey.value)
    )

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
            SettingsAction.OnRemoveKeyClick -> isRemoveKeyDialogOpen.value = true
            SettingsAction.OnRemoveKeyDismiss -> isRemoveKeyDialogOpen.value = false
            SettingsAction.OnRemoveKeyConfirm -> {
                assistant.removeKey()
                isRemoveKeyDialogOpen.value = false
            }
        }
    }
}
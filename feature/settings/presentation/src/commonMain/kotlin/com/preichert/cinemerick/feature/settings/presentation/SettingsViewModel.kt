package com.preichert.cinemerick.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.presentation.AnimationController
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.presentation.TimeWindow
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.domain.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val themeController: ThemeController,
    private val animationController: AnimationController,
    private val timeWindowController: TimeWindowController,
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        themeController.mode,
        animationController.isEnabled,
        timeWindowController.window,
        ::toState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = toState(
            themeController.mode.value,
            animationController.isEnabled.value,
            timeWindowController.window.value
        )
    )

    private fun toState(themeMode: ThemeMode, animationsEnabled: Boolean, window: TimeWindow) = SettingsState(
        themeMode = themeMode,
        animationsEnabled = animationsEnabled,
        defaultMinMinutes = window.minMinutes,
        defaultMaxMinutes = window.maxMinutes,
    )

    fun onAction(action: SettingsAction) {
        when (action) {
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
        }
    }
}
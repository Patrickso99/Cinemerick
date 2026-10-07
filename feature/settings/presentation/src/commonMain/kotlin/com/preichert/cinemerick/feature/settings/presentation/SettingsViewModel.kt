package com.preichert.cinemerick.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.domain.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val themeController: ThemeController,
) : ViewModel() {

    val state: StateFlow<SettingsState> = themeController.mode
        .map { themeMode ->
            SettingsState(themeMode = themeMode)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = SettingsState(themeMode = themeController.mode.value)
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
        }
    }
}
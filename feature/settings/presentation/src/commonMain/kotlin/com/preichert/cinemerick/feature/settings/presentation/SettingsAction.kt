package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.domain.ThemeMode

sealed interface SettingsAction {
    data class OnAutoToggled(val enabled: Boolean, val systemDark: Boolean) : SettingsAction
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsAction
}
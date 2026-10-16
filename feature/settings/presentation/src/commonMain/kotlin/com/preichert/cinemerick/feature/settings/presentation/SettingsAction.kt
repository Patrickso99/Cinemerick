package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.ThemeMode

sealed interface SettingsAction {
    data class OnAutoToggled(val enabled: Boolean, val systemDark: Boolean) : SettingsAction
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsAction
    data class OnAnimationsToggled(val enabled: Boolean) : SettingsAction
    data class OnDefaultMinTimeChanged(val minutes: Int) : SettingsAction
    data class OnDefaultMaxTimeChanged(val minutes: Int?) : SettingsAction
    data class OnLanguageSelected(val language: AppLanguage) : SettingsAction
}
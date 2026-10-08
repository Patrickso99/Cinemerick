package com.preichert.cinemerick.feature.settings.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.domain.ThemeMode

@Stable
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val animationsEnabled: Boolean = true,
)
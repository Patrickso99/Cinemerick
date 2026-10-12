package com.preichert.cinemerick.feature.settings.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.domain.DEFAULT_MIN_MINUTES
import com.preichert.cinemerick.core.domain.ThemeMode

@Stable
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val animationsEnabled: Boolean = true,
    val defaultMinMinutes: Int = DEFAULT_MIN_MINUTES,
    val defaultMaxMinutes: Int? = null,
)
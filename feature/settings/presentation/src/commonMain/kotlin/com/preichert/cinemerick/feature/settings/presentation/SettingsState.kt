package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.domain.DEFAULT_MIN_MINUTES
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.domain.assistant.SavedKey

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val animationsEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val defaultMinMinutes: Int = DEFAULT_MIN_MINUTES,
    val defaultMaxMinutes: Int? = null,
    // The assistant's key, masked; null until one is saved.
    val assistantKey: SavedKey? = null,
    val isRemoveKeyDialogOpen: Boolean = false,
)
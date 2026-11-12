package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.domain.DEFAULT_MIN_MINUTES
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.domain.assistant.AssistantEngine
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.core.domain.assistant.SystemAiState

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val animationsEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val defaultMinMinutes: Int = DEFAULT_MIN_MINUTES,
    val defaultMaxMinutes: Int? = null,
    // The assistant's key, masked; null until one is saved.
    val assistantKey: SavedKey? = null,
    // The AI of the system on this device; its name is null where the platform has none (no row for it then).
    val systemAi: SystemAiState = SystemAiState(),
    // The engine that answers now, and the one the user chose (it may not be ready: Apple Intelligence turned off).
    val activeEngine: AssistantEngine? = null,
    val chosenEngine: AssistantEngine? = null,
    val isRemoveKeyDialogOpen: Boolean = false,
) {
    /** The engine the radio of the assistant section marks: the one answering, or else the one chosen. */
    val selectedEngine: AssistantEngine? get() = activeEngine ?: chosenEngine
}

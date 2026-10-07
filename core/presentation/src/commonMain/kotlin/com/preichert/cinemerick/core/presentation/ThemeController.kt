package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.domain.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
class ThemeController(
    private val themePreferences: ThemePreferences,
) {
    private val _mode = MutableStateFlow(themePreferences.themeMode())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(themeMode: ThemeMode) {
        _mode.value = themeMode
        themePreferences.setThemeMode(themeMode)
    }
}
package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.domain.ThemePreferences
import com.russhwolf.settings.Settings

private const val KEY_THEME_MODE = "theme_mode"

class SettingsThemePreferences(private val settings: Settings) : ThemePreferences {

    override fun themeMode(): ThemeMode {
        val value = settings.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.name)
    }
}
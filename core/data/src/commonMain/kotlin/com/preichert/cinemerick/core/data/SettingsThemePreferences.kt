package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.ThemePreferences
import com.russhwolf.settings.Settings

class SettingsThemePreferences(private val settings: Settings) : ThemePreferences {

    override fun darkTheme(): Boolean? = settings.getBooleanOrNull(KEY_DARK_THEME)

    override fun setDarkTheme(dark: Boolean) {
        settings.putBoolean(KEY_DARK_THEME, dark)
    }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
    }
}

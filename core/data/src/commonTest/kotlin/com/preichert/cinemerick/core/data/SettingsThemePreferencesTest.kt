package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsThemePreferencesTest {

    @Test
    fun followsSystemByDefault() {
        val preferences = SettingsThemePreferences(MemorySettings())

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode())
    }

    @Test
    fun remembersLightAndDarkAndSystem() {
        val preferences = SettingsThemePreferences(MemorySettings())

        preferences.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, preferences.themeMode())

        preferences.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, preferences.themeMode())

        preferences.setThemeMode(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode())
    }
}

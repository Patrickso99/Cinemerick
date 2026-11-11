package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo

class SettingsThemePreferencesTest {

    @Test
    fun followsSystemByDefault() {
        val preferences = SettingsThemePreferences(MemorySettings())

        assertThat(preferences.themeMode()).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun remembersLightAndDarkAndSystem() {
        val preferences = SettingsThemePreferences(MemorySettings())

        preferences.setThemeMode(ThemeMode.LIGHT)
        assertThat(preferences.themeMode()).isEqualTo(ThemeMode.LIGHT)

        preferences.setThemeMode(ThemeMode.DARK)
        assertThat(preferences.themeMode()).isEqualTo(ThemeMode.DARK)

        preferences.setThemeMode(ThemeMode.SYSTEM)
        assertThat(preferences.themeMode()).isEqualTo(ThemeMode.SYSTEM)
    }
}

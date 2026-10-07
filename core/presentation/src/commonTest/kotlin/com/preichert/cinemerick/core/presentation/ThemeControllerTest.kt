package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.data.SettingsThemePreferencesTest.MemorySettings
import com.preichert.cinemerick.core.data.SettingsThemePreferences
import com.preichert.cinemerick.core.domain.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeControllerTest {

    @Test
    fun seedsInitialValueFromPreferences() = runTest {
        val preferences = SettingsThemePreferences(MemorySettings())
        preferences.setThemeMode(ThemeMode.DARK)

        val controller = ThemeController(preferences)

        assertEquals(ThemeMode.DARK, controller.mode.value)
    }

    @Test
    fun setModeUpdatesFlowAndPersists() = runTest {
        val preferences = SettingsThemePreferences(MemorySettings())
        val controller = ThemeController(preferences)

        controller.setMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, controller.mode.value)
        assertEquals(ThemeMode.LIGHT, preferences.themeMode())
    }

    @Test
    fun systemIsDefaultMode() = runTest {
        val preferences = SettingsThemePreferences(MemorySettings())
        val controller = ThemeController(preferences)

        assertEquals(ThemeMode.SYSTEM, controller.mode.value)
    }

    @Test
    fun flowEmitsUpdatesAcrossAllModes() = runTest {
        val preferences = SettingsThemePreferences(MemorySettings())
        val controller = ThemeController(preferences)

        val values = mutableListOf<ThemeMode>()
        val job = kotlinx.coroutines.launch {
            controller.mode.collect { values.add(it) }
        }

        controller.setMode(ThemeMode.LIGHT)
        controller.setMode(ThemeMode.DARK)
        controller.setMode(ThemeMode.SYSTEM)

        job.cancel()

        assertEquals(
            listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM),
            values
        )
    }
}
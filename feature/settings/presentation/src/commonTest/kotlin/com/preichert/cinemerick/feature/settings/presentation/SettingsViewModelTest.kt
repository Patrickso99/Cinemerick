package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.data.SettingsThemePreferencesTest.MemorySettings
import com.preichert.cinemerick.core.data.SettingsThemePreferences
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.presentation.ThemeController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {

    @Test
    fun onAutoToggledWithEnabledTrueSwitchesToSystem() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller)

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = true, systemDark = false))

        assertEquals(ThemeMode.SYSTEM, controller.mode.value)
    }

    @Test
    fun onAutoToggledWithEnabledFalseAndSystemDarkTrueSwitchesToDark() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller)

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = false, systemDark = true))

        assertEquals(ThemeMode.DARK, controller.mode.value)
    }

    @Test
    fun onAutoToggledWithEnabledFalseAndSystemDarkFalseSwitchesToLight() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller)

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = false, systemDark = false))

        assertEquals(ThemeMode.LIGHT, controller.mode.value)
    }

    @Test
    fun onThemeModeSelectedUpdatesMode() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller)

        viewModel.onAction(SettingsAction.OnThemeModeSelected(ThemeMode.DARK))

        assertEquals(ThemeMode.DARK, controller.mode.value)
    }

    @Test
    fun stateReflectsControllerMode() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        controller.setMode(ThemeMode.LIGHT)

        val viewModel = SettingsViewModel(controller)

        val state = viewModel.state.first()
        assertEquals(ThemeMode.LIGHT, state.themeMode)
    }
}
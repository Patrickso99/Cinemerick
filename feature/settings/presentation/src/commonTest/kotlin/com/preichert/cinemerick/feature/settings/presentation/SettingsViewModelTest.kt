package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.testing.MemorySettings
import com.preichert.cinemerick.core.data.SettingsThemePreferences
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.presentation.AnimationController
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.data.SettingsTimeWindowPreferences
import com.preichert.cinemerick.core.data.SettingsAnimationPreferences
import com.preichert.cinemerick.core.data.SettingsLanguagePreferences
import com.preichert.cinemerick.core.presentation.LanguageController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsViewModelTest {

    private fun timeWindow() = TimeWindowController(SettingsTimeWindowPreferences(MemorySettings()))
    private fun languageController() = LanguageController(SettingsLanguagePreferences(MemorySettings()))

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAutoToggledWithEnabledTrueSwitchesToSystem() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller, AnimationController(SettingsAnimationPreferences(MemorySettings())), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = true, systemDark = false))

        assertEquals(ThemeMode.SYSTEM, controller.mode.value)
    }

    @Test
    fun onAutoToggledWithEnabledFalseAndSystemDarkTrueSwitchesToDark() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller, AnimationController(SettingsAnimationPreferences(MemorySettings())), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = false, systemDark = true))

        assertEquals(ThemeMode.DARK, controller.mode.value)
    }

    @Test
    fun onAutoToggledWithEnabledFalseAndSystemDarkFalseSwitchesToLight() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller, AnimationController(SettingsAnimationPreferences(MemorySettings())), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = false, systemDark = false))

        assertEquals(ThemeMode.LIGHT, controller.mode.value)
    }

    @Test
    fun onThemeModeSelectedUpdatesMode() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        val viewModel = SettingsViewModel(controller, AnimationController(SettingsAnimationPreferences(MemorySettings())), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnThemeModeSelected(ThemeMode.DARK))

        assertEquals(ThemeMode.DARK, controller.mode.value)
    }

    @Test
    fun stateReflectsControllerMode() = runTest {
        val controller = ThemeController(SettingsThemePreferences(MemorySettings()))
        controller.setMode(ThemeMode.LIGHT)

        val viewModel = SettingsViewModel(controller, AnimationController(SettingsAnimationPreferences(MemorySettings())), timeWindow(), languageController())

        val state = viewModel.state.first()
        assertEquals(ThemeMode.LIGHT, state.themeMode)
    }

    @Test
    fun onAnimationsToggledUpdatesControllerAndState() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val animations = AnimationController(SettingsAnimationPreferences(MemorySettings()))
        val viewModel = SettingsViewModel(ThemeController(SettingsThemePreferences(MemorySettings())), animations, timeWindow(), languageController())

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        viewModel.onAction(SettingsAction.OnAnimationsToggled(false))

        assertEquals(false, animations.isEnabled.value)
        assertEquals(false, viewModel.state.value.animationsEnabled)
    }

    @Test
    fun validWindowChangesAreApplied() = runTest {
        val window = timeWindow()
        val viewModel = SettingsViewModel(ThemeController(SettingsThemePreferences(MemorySettings())), AnimationController(SettingsAnimationPreferences(MemorySettings())), window, languageController())

        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(23 * 60))
        viewModel.onAction(SettingsAction.OnDefaultMinTimeChanged(18 * 60))

        assertEquals(18 * 60, window.window.value.minMinutes)
        assertEquals(23 * 60, window.window.value.maxMinutes)
    }

    @Test
    fun invalidWindowChangesAreRejected() = runTest {
        val window = timeWindow()
        val viewModel = SettingsViewModel(ThemeController(SettingsThemePreferences(MemorySettings())), AnimationController(SettingsAnimationPreferences(MemorySettings())), window, languageController())
        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(22 * 60))

        viewModel.onAction(SettingsAction.OnDefaultMinTimeChanged(23 * 60))
        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(19 * 60))

        assertEquals(20 * 60, window.window.value.minMinutes)
        assertEquals(22 * 60, window.window.value.maxMinutes)
    }

    @Test
    fun noLimitClearsMax() = runTest {
        val window = timeWindow()
        val viewModel = SettingsViewModel(ThemeController(SettingsThemePreferences(MemorySettings())), AnimationController(SettingsAnimationPreferences(MemorySettings())), window, languageController())
        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(23 * 60))

        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(null))

        assertNull(window.window.value.maxMinutes)
    }
}

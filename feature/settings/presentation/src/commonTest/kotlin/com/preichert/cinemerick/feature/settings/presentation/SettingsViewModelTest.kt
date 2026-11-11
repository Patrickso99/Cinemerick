package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.presentation.AnimationController
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.presentation.LanguageController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import com.preichert.cinemerick.core.testing.FakeAnimationPreferences
import com.preichert.cinemerick.core.testing.FakeLanguagePreferences
import com.preichert.cinemerick.core.testing.FakeThemePreferences
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences

class SettingsViewModelTest {

    private fun timeWindow() = TimeWindowController(FakeTimeWindowPreferences())
    private fun languageController() = LanguageController(FakeLanguagePreferences())

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onAutoToggledWithEnabledTrueSwitchesToSystem() = runTest {
        val controller = ThemeController(FakeThemePreferences())
        val viewModel = SettingsViewModel(controller, AnimationController(FakeAnimationPreferences()), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = true, systemDark = false))

        assertThat(controller.mode.value).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun onAutoToggledWithEnabledFalseAndSystemDarkTrueSwitchesToDark() = runTest {
        val controller = ThemeController(FakeThemePreferences())
        val viewModel = SettingsViewModel(controller, AnimationController(FakeAnimationPreferences()), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = false, systemDark = true))

        assertThat(controller.mode.value).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun onAutoToggledWithEnabledFalseAndSystemDarkFalseSwitchesToLight() = runTest {
        val controller = ThemeController(FakeThemePreferences())
        val viewModel = SettingsViewModel(controller, AnimationController(FakeAnimationPreferences()), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnAutoToggled(enabled = false, systemDark = false))

        assertThat(controller.mode.value).isEqualTo(ThemeMode.LIGHT)
    }

    @Test
    fun onThemeModeSelectedUpdatesMode() = runTest {
        val controller = ThemeController(FakeThemePreferences())
        val viewModel = SettingsViewModel(controller, AnimationController(FakeAnimationPreferences()), timeWindow(), languageController())

        viewModel.onAction(SettingsAction.OnThemeModeSelected(ThemeMode.DARK))

        assertThat(controller.mode.value).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun stateReflectsControllerMode() = runTest {
        val controller = ThemeController(FakeThemePreferences())
        controller.setMode(ThemeMode.LIGHT)

        val viewModel = SettingsViewModel(controller, AnimationController(FakeAnimationPreferences()), timeWindow(), languageController())

        viewModel.state.test {
            assertThat(awaitItem().themeMode).isEqualTo(ThemeMode.LIGHT)
        }
    }

    @Test
    fun onAnimationsToggledUpdatesControllerAndState() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val animations = AnimationController(FakeAnimationPreferences())
        val viewModel = SettingsViewModel(ThemeController(FakeThemePreferences()), animations, timeWindow(), languageController())

        viewModel.state.test {
            viewModel.onAction(SettingsAction.OnAnimationsToggled(false))

            assertThat(animations.isEnabled.value).isFalse()
            assertThat(expectMostRecentItem().animationsEnabled).isFalse()
        }
    }

    @Test
    fun validWindowChangesAreApplied() = runTest {
        val window = timeWindow()
        val viewModel = SettingsViewModel(ThemeController(FakeThemePreferences()), AnimationController(FakeAnimationPreferences()), window, languageController())

        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(23 * 60))
        viewModel.onAction(SettingsAction.OnDefaultMinTimeChanged(18 * 60))

        assertThat(window.window.value.minMinutes).isEqualTo(18 * 60)
        assertThat(window.window.value.maxMinutes).isEqualTo(23 * 60)
    }

    @Test
    fun invalidWindowChangesAreRejected() = runTest {
        val window = timeWindow()
        val viewModel = SettingsViewModel(ThemeController(FakeThemePreferences()), AnimationController(FakeAnimationPreferences()), window, languageController())
        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(22 * 60))

        viewModel.onAction(SettingsAction.OnDefaultMinTimeChanged(23 * 60))
        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(19 * 60))

        assertThat(window.window.value.minMinutes).isEqualTo(20 * 60)
        assertThat(window.window.value.maxMinutes).isEqualTo(22 * 60)
    }

    @Test
    fun noLimitClearsMax() = runTest {
        val window = timeWindow()
        val viewModel = SettingsViewModel(ThemeController(FakeThemePreferences()), AnimationController(FakeAnimationPreferences()), window, languageController())
        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(23 * 60))

        viewModel.onAction(SettingsAction.OnDefaultMaxTimeChanged(null))

        assertThat(window.window.value.maxMinutes).isNull()
    }
}

package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.ThemeMode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.preichert.cinemerick.core.testing.FakeThemePreferences

class ThemeControllerTest {

    @Test
    fun seedsInitialValueFromPreferences() = runTest {
        val preferences = FakeThemePreferences()
        preferences.setThemeMode(ThemeMode.DARK)

        val controller = ThemeController(preferences)

        assertThat(controller.mode.value).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun setModeUpdatesFlowAndPersists() = runTest {
        val preferences = FakeThemePreferences()
        val controller = ThemeController(preferences)

        controller.setMode(ThemeMode.LIGHT)

        assertThat(controller.mode.value).isEqualTo(ThemeMode.LIGHT)
        assertThat(preferences.themeMode()).isEqualTo(ThemeMode.LIGHT)
    }

    @Test
    fun systemIsDefaultMode() = runTest {
        val preferences = FakeThemePreferences()
        val controller = ThemeController(preferences)

        assertThat(controller.mode.value).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun flowEmitsUpdatesAcrossAllModes() = runTest {
        val preferences = FakeThemePreferences()
        val controller = ThemeController(preferences)

        controller.mode.test {
            assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
            controller.setMode(ThemeMode.LIGHT)
            assertThat(awaitItem()).isEqualTo(ThemeMode.LIGHT)
            controller.setMode(ThemeMode.DARK)
            assertThat(awaitItem()).isEqualTo(ThemeMode.DARK)
            controller.setMode(ThemeMode.SYSTEM)
            assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
        }
    }
}
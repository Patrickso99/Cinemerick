package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue

class SettingsAnimationPreferencesTest {

    @Test
    fun enabledByDefault() {
        val preferences = SettingsAnimationPreferences(MemorySettings())

        assertThat(preferences.animationsEnabled()).isTrue()
    }

    @Test
    fun remembersDisabledAndEnabled() {
        val preferences = SettingsAnimationPreferences(MemorySettings())

        preferences.setAnimationsEnabled(false)
        assertThat(preferences.animationsEnabled()).isFalse()

        preferences.setAnimationsEnabled(true)
        assertThat(preferences.animationsEnabled()).isTrue()
    }
}

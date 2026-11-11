package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

class SettingsTimeWindowPreferencesTest {

    @Test
    fun defaultsToEightPmAndNoLimit() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())

        assertThat(preferences.minMinutes()).isEqualTo(20 * 60)
        assertThat(preferences.maxMinutes()).isNull()
    }

    @Test
    fun valuesRoundTrip() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())

        preferences.setMinMinutes(18 * 60 + 30)
        preferences.setMaxMinutes(23 * 60)

        assertThat(preferences.minMinutes()).isEqualTo(18 * 60 + 30)
        assertThat(preferences.maxMinutes()).isEqualTo(23 * 60)
    }

    @Test
    fun removingMaxRestoresNoLimit() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())
        preferences.setMaxMinutes(23 * 60)

        preferences.setMaxMinutes(null)

        assertThat(preferences.maxMinutes()).isNull()
    }
}

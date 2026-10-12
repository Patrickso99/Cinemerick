package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsTimeWindowPreferencesTest {

    @Test
    fun defaultsToEightPmAndNoLimit() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())

        assertEquals(20 * 60, preferences.minMinutes())
        assertNull(preferences.maxMinutes())
    }

    @Test
    fun valuesRoundTrip() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())

        preferences.setMinMinutes(18 * 60 + 30)
        preferences.setMaxMinutes(23 * 60)

        assertEquals(18 * 60 + 30, preferences.minMinutes())
        assertEquals(23 * 60, preferences.maxMinutes())
    }

    @Test
    fun removingMaxRestoresNoLimit() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())
        preferences.setMaxMinutes(23 * 60)

        preferences.setMaxMinutes(null)

        assertNull(preferences.maxMinutes())
    }
}

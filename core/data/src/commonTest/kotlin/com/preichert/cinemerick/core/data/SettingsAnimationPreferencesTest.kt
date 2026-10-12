package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsAnimationPreferencesTest {

    @Test
    fun enabledByDefault() {
        val preferences = SettingsAnimationPreferences(MemorySettings())

        assertEquals(true, preferences.animationsEnabled())
    }

    @Test
    fun remembersDisabledAndEnabled() {
        val preferences = SettingsAnimationPreferences(MemorySettings())

        preferences.setAnimationsEnabled(false)
        assertEquals(false, preferences.animationsEnabled())

        preferences.setAnimationsEnabled(true)
        assertEquals(true, preferences.animationsEnabled())
    }
}

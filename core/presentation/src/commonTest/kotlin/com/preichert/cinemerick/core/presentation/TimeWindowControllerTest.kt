package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.testing.MemorySettings
import com.preichert.cinemerick.core.data.SettingsTimeWindowPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeWindowControllerTest {

    private fun controller(preferences: SettingsTimeWindowPreferences = SettingsTimeWindowPreferences(MemorySettings())) =
        TimeWindowController(preferences)

    @Test
    fun startsWithDefaults() {
        val window = controller().window.value

        assertEquals(20 * 60, window.minMinutes)
        assertNull(window.maxMinutes)
    }

    @Test
    fun rejectsMinAboveMax() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())
        val controller = controller(preferences)
        controller.setMax(22 * 60)

        assertFalse(controller.setMin(23 * 60))

        assertEquals(20 * 60, controller.window.value.minMinutes)
        assertEquals(20 * 60, preferences.minMinutes())
    }

    @Test
    fun rejectsMaxBelowMin() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())
        val controller = controller(preferences)

        assertFalse(controller.setMax(19 * 60))

        assertNull(controller.window.value.maxMinutes)
        assertNull(preferences.maxMinutes())
    }

    @Test
    fun acceptsEqualBounds() {
        val controller = controller()

        assertTrue(controller.setMax(20 * 60))
    }

    @Test
    fun persistsAcceptedValuesAndNoLimit() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())
        val controller = controller(preferences)

        assertTrue(controller.setMin(18 * 60))
        assertTrue(controller.setMax(23 * 60))
        assertEquals(TimeWindow(18 * 60, 23 * 60), controller.window.value)
        assertEquals(18 * 60, preferences.minMinutes())
        assertEquals(23 * 60, preferences.maxMinutes())

        assertTrue(controller.setMax(null))
        assertNull(preferences.maxMinutes())
    }

    @Test
    fun seedsFromStoredValues() {
        val preferences = SettingsTimeWindowPreferences(MemorySettings())
        preferences.setMinMinutes(18 * 60)
        preferences.setMaxMinutes(23 * 60)

        assertEquals(TimeWindow(18 * 60, 23 * 60), controller(preferences).window.value)
    }

    @Test
    fun formatsMinutes() {
        assertEquals("00:00", 0.toTimeString())
        assertEquals("23:59", (23 * 60 + 59).toTimeString())
        assertEquals("08:05", (8 * 60 + 5).toTimeString())
        assertEquals("", null.toTimeInput())
        assertEquals("18:30", (18 * 60 + 30).toTimeInput())
    }

    @Test
    fun hourMinuteRoundTrip() {
        val minutes = 18 * 60 + 45
        assertEquals(minutes, minutesOf(minutes.toHour(), minutes.toMinute()))
        assertEquals(0, minutesOf(0, 0))
        assertEquals(23 * 60 + 59, minutesOf(23, 59))
    }
}

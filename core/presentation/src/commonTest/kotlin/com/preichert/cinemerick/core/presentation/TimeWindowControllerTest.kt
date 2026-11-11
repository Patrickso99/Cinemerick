package com.preichert.cinemerick.core.presentation

import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.preichert.cinemerick.core.domain.TimeWindowPreferences
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences

class TimeWindowControllerTest {

    private fun controller(preferences: TimeWindowPreferences = FakeTimeWindowPreferences()) =
        TimeWindowController(preferences)

    @Test
    fun startsWithDefaults() {
        val window = controller().window.value

        assertThat(window.minMinutes).isEqualTo(20 * 60)
        assertThat(window.maxMinutes).isNull()
    }

    @Test
    fun rejectsMinAboveMax() {
        val preferences = FakeTimeWindowPreferences()
        val controller = controller(preferences)
        controller.setMax(22 * 60)

        assertThat(controller.setMin(23 * 60)).isFalse()

        assertThat(controller.window.value.minMinutes).isEqualTo(20 * 60)
        assertThat(preferences.minMinutes()).isEqualTo(20 * 60)
    }

    @Test
    fun rejectsMaxBelowMin() {
        val preferences = FakeTimeWindowPreferences()
        val controller = controller(preferences)

        assertThat(controller.setMax(19 * 60)).isFalse()

        assertThat(controller.window.value.maxMinutes).isNull()
        assertThat(preferences.maxMinutes()).isNull()
    }

    @Test
    fun acceptsEqualBounds() {
        val controller = controller()

        assertThat(controller.setMax(20 * 60)).isTrue()
    }

    @Test
    fun persistsAcceptedValuesAndNoLimit() {
        val preferences = FakeTimeWindowPreferences()
        val controller = controller(preferences)

        assertThat(controller.setMin(18 * 60)).isTrue()
        assertThat(controller.setMax(23 * 60)).isTrue()
        assertThat(controller.window.value).isEqualTo(TimeWindow(18 * 60, 23 * 60))
        assertThat(preferences.minMinutes()).isEqualTo(18 * 60)
        assertThat(preferences.maxMinutes()).isEqualTo(23 * 60)

        assertThat(controller.setMax(null)).isTrue()
        assertThat(preferences.maxMinutes()).isNull()
    }

    @Test
    fun seedsFromStoredValues() {
        val preferences = FakeTimeWindowPreferences()
        preferences.setMinMinutes(18 * 60)
        preferences.setMaxMinutes(23 * 60)

        assertThat(controller(preferences).window.value).isEqualTo(TimeWindow(18 * 60, 23 * 60))
    }

    @Test
    fun formatsMinutes() {
        assertThat(0.toTimeString()).isEqualTo("00:00")
        assertThat((23 * 60 + 59).toTimeString()).isEqualTo("23:59")
        assertThat((8 * 60 + 5).toTimeString()).isEqualTo("08:05")
        assertThat(null.toTimeInput()).isEqualTo("")
        assertThat((18 * 60 + 30).toTimeInput()).isEqualTo("18:30")
    }

    @Test
    fun hourMinuteRoundTrip() {
        val minutes = 18 * 60 + 45
        assertThat(minutesOf(minutes.toHour(), minutes.toMinute())).isEqualTo(minutes)
        assertThat(minutesOf(0, 0)).isEqualTo(0)
        assertThat(minutesOf(23, 59)).isEqualTo(23 * 60 + 59)
    }
}

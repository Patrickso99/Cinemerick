package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.domain.TimeWindowPreferences
import com.preichert.cinemerick.core.domain.isValidWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Default start time window, in minutes since midnight; a null [maxMinutes] means no limit. */
data class TimeWindow(val minMinutes: Int, val maxMinutes: Int?)

@Stable
class TimeWindowController(
    private val preferences: TimeWindowPreferences,
) {
    private val _window = MutableStateFlow(TimeWindow(preferences.minMinutes(), preferences.maxMinutes()))
    val window: StateFlow<TimeWindow> = _window.asStateFlow()

    /** Returns false and leaves the stored window untouched when the result would be invalid. */
    fun setMin(minutes: Int): Boolean {
        val current = _window.value
        if (!isValidWindow(minutes, current.maxMinutes)) return false
        preferences.setMinMinutes(minutes)
        _window.value = current.copy(minMinutes = minutes)
        return true
    }

    /** Returns false and leaves the stored window untouched when the result would be invalid. */
    fun setMax(minutes: Int?): Boolean {
        val current = _window.value
        if (!isValidWindow(current.minMinutes, minutes)) return false
        preferences.setMaxMinutes(minutes)
        _window.value = current.copy(maxMinutes = minutes)
        return true
    }
}

fun minutesOf(hour: Int, minute: Int): Int = hour * 60 + minute

fun Int.toHour(): Int = this / 60

fun Int.toMinute(): Int = this % 60

fun Int.toTimeString(): String = "${toHour().toString().padStart(2, '0')}:${toMinute().toString().padStart(2, '0')}"

/** Text for the Showtimes day inputs: a missing maximum stays blank (no limit). */
fun Int?.toTimeInput(): String = this?.toTimeString() ?: ""

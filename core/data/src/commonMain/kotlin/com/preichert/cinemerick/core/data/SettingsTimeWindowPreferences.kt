package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.DEFAULT_MIN_MINUTES
import com.preichert.cinemerick.core.domain.TimeWindowPreferences
import com.russhwolf.settings.Settings

private const val KEY_DEFAULT_MIN_TIME = "default_min_time"
private const val KEY_DEFAULT_MAX_TIME = "default_max_time"

class SettingsTimeWindowPreferences(private val settings: Settings) : TimeWindowPreferences {

    override fun minMinutes(): Int = settings.getInt(KEY_DEFAULT_MIN_TIME, DEFAULT_MIN_MINUTES)

    override fun maxMinutes(): Int? = settings.getIntOrNull(KEY_DEFAULT_MAX_TIME)

    override fun setMinMinutes(minutes: Int) {
        settings.putInt(KEY_DEFAULT_MIN_TIME, minutes)
    }

    override fun setMaxMinutes(minutes: Int?) {
        if (minutes == null) settings.remove(KEY_DEFAULT_MAX_TIME) else settings.putInt(KEY_DEFAULT_MAX_TIME, minutes)
    }
}

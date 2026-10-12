package com.preichert.cinemerick.core.domain

/** Default start time window applied to newly selected days, in minutes since midnight. */
interface TimeWindowPreferences {
    fun minMinutes(): Int
    fun maxMinutes(): Int?
    fun setMinMinutes(minutes: Int)
    fun setMaxMinutes(minutes: Int?)
}

const val DEFAULT_MIN_MINUTES = 20 * 60

/** A window is valid when it has no maximum or when the minimum does not exceed the maximum. */
fun isValidWindow(min: Int, max: Int?): Boolean = max == null || min <= max

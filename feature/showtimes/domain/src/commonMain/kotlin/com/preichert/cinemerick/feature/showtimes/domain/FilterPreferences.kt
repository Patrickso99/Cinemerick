package com.preichert.cinemerick.feature.showtimes.domain

/** Persists the user's filter choices between sessions. */
interface FilterPreferences {
    fun getHiddenTags(): Set<String>
    fun setHiddenTags(tags: Set<String>)

    /** Null when the user never chose: callers fall back to all cinemas. */
    fun getSelectedCinemas(): Set<Cinema>?
    fun setSelectedCinemas(cinemas: Set<Cinema>)
}

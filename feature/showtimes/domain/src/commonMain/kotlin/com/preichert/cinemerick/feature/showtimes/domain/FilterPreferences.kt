package com.preichert.cinemerick.feature.showtimes.domain

/** Persists the user's filter choices between sessions. */
interface FilterPreferences {
    /** Null when the user never chose: callers start with empty selection. */
    fun getSelectedVenues(): Set<Venue>?
    fun setSelectedVenues(venues: Set<Venue>)
}

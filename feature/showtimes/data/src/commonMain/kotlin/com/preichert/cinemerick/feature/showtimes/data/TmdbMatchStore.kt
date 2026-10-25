package com.preichert.cinemerick.feature.showtimes.data

import com.russhwolf.settings.Settings
import kotlin.time.Duration.Companion.days

private const val KEY_PREFIX = "tmdb_match."
private const val NO_MATCH = "none"
private const val FIELD_SEPARATOR = "|"

// A film can reach TMDB after it was first searched, so a "no match" is asked again after a week. A match never expires.
private val NO_MATCH_TTL_MILLIS = 7.days.inWholeMilliseconds

/** What the film database answered for a title, remembered across launches. */
sealed interface RememberedMatch {
    data class Found(val movieId: Int) : RememberedMatch
    data object NoMatch : RememberedMatch
}

/** Remembers the TMDB answer for each title, keyed by its `titleKey`. */
interface TmdbMatchStore {
    /** The remembered answer, or null when nothing is remembered or a remembered "no match" has expired. */
    fun get(titleKey: String): RememberedMatch?

    fun put(titleKey: String, match: RememberedMatch)
}

class SettingsTmdbMatchStore(
    private val settings: Settings,
    private val now: () -> Long = ::getCurrentTimeMillis
) : TmdbMatchStore {

    // Stored as "<movie id or none>|<time saved>". A value that does not parse is ignored, as if nothing was saved.
    override fun get(titleKey: String): RememberedMatch? {
        val fields = settings.getStringOrNull(KEY_PREFIX + titleKey)?.split(FIELD_SEPARATOR) ?: return null
        val savedAt = fields.getOrNull(1)?.toLongOrNull() ?: return null
        return when (fields[0]) {
            NO_MATCH -> RememberedMatch.NoMatch.takeIf { now() - savedAt <= NO_MATCH_TTL_MILLIS }
            else -> fields[0].toIntOrNull()?.let { RememberedMatch.Found(it) }
        }
    }

    override fun put(titleKey: String, match: RememberedMatch) {
        val value = when (match) {
            is RememberedMatch.Found -> match.movieId.toString()
            RememberedMatch.NoMatch -> NO_MATCH
        }
        settings.putString(KEY_PREFIX + titleKey, listOf(value, now()).joinToString(FIELD_SEPARATOR))
    }
}

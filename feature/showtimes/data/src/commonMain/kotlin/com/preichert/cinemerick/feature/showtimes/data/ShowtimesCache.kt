package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class CacheEntry<T>(
    val data: T,
    val timestamp: Long
)

class ShowtimesCache(
    private val ttlMillis: Long = 5.minutes.inWholeMilliseconds,
    private val clock: Clock = Clock.System
) {
    private val cache = mutableMapOf<String, CacheEntry<List<Showing>>>()

    private fun cacheKey(venue: Venue, day: LocalDate): String =
        "${venue.chain}:${venue.id}:$day"

    fun get(venue: Venue, day: LocalDate): List<Showing>? {
        val key = cacheKey(venue, day)
        val entry = cache[key] ?: return null

        val now = clock.now().toEpochMilliseconds()
        val age = now - entry.timestamp
        if (age > ttlMillis) {
            cache.remove(key)
            return null
        }

        return entry.data
    }

    fun put(venue: Venue, day: LocalDate, showings: List<Showing>) {
        val key = cacheKey(venue, day)
        cache[key] = CacheEntry(showings, clock.now().toEpochMilliseconds())
    }

    fun clear() {
        cache.clear()
    }

    fun clearStale() {
        val now = clock.now().toEpochMilliseconds()
        cache.entries.removeAll { (_, entry) ->
            (now - entry.timestamp) > ttlMillis
        }
    }
}

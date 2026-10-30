package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.minutes

data class CacheEntry<T>(
    val data: T,
    val timestamp: Long
)

class ShowtimesCache(
    private val ttlMillis: Long = 5.minutes.inWholeMilliseconds
) {
    // Written from concurrent fetches of several venues, so every access goes through the lock.
    private val cache = mutableMapOf<String, CacheEntry<List<Showing>>>()
    private val lock = Mutex()

    private fun cacheKey(venue: Venue, day: LocalDate): String =
        "${venue.chain}:${venue.id}:$day"

    suspend fun get(venue: Venue, day: LocalDate): List<Showing>? = lock.withLock {
        val key = cacheKey(venue, day)
        val entry = cache[key] ?: return@withLock null

        val now = getCurrentTimeMillis()
        val age = now - entry.timestamp
        if (age > ttlMillis) {
            cache.remove(key)
            return@withLock null
        }

        entry.data
    }

    suspend fun put(venue: Venue, day: LocalDate, showings: List<Showing>) = lock.withLock {
        val key = cacheKey(venue, day)
        cache[key] = CacheEntry(showings, getCurrentTimeMillis())
    }

    suspend fun clear() = lock.withLock {
        cache.clear()
    }

    suspend fun clearStale() = lock.withLock {
        val now = getCurrentTimeMillis()
        cache.entries.removeAll { (_, entry) ->
            (now - entry.timestamp) > ttlMillis
        }
    }
}

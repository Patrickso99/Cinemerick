package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShowtimesCacheTest {

    private val day = LocalDate(2026, 11, 1)

    private fun venue(index: Int) = Venue(Chain.UCI, "venue-$index", "Venue $index")

    private fun showing(venue: Venue) = Showing("Film", day, LocalTime(20, 30), venue)

    @Test
    fun aPutIsReadBackForTheSameVenueAndDay() = runTest {
        val cache = ShowtimesCache()
        val venue = venue(1)

        cache.put(venue, day, listOf(showing(venue)))

        assertEquals(listOf(showing(venue)), cache.get(venue, day))
    }

    @Test
    fun aDifferentDayOrVenueIsAMiss() = runTest {
        val cache = ShowtimesCache()
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        assertNull(cache.get(venue, day.plus(1, DateTimeUnit.DAY)))
        assertNull(cache.get(venue(2), day))
    }

    @Test
    fun anEntryOlderThanTheTtlIsDropped() = runTest {
        val cache = ShowtimesCache(ttlMillis = -1)
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        assertNull(cache.get(venue, day))
    }

    @Test
    fun writesFromConcurrentFetchesAreAllKept() = runTest {
        val cache = ShowtimesCache()

        (1..200).map { index ->
            async(Dispatchers.Default) { cache.put(venue(index), day, listOf(showing(venue(index)))) }
        }.awaitAll()

        (1..200).forEach { index -> assertEquals(1, cache.get(venue(index), day)?.size) }
    }

    @Test
    fun clearEmptiesTheCache() = runTest {
        val cache = ShowtimesCache()
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        cache.clear()

        assertNull(cache.get(venue, day))
    }
}

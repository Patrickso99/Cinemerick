package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.testing.FakeClock
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
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

class ShowtimesCacheTest {

    private val day = LocalDate(2026, 11, 1)

    private fun venue(index: Int) = Venue(Chain.UCI, "venue-$index", "Venue $index")

    private fun showing(venue: Venue) = Showing("Film", day, LocalTime(20, 30), venue)

    @Test
    fun aPutIsReadBackForTheSameVenueAndDay() = runTest {
        val cache = ShowtimesCache(FakeClock())
        val venue = venue(1)

        cache.put(venue, day, listOf(showing(venue)))

        assertThat(cache.get(venue, day)).isEqualTo(listOf(showing(venue)))
    }

    @Test
    fun aDifferentDayOrVenueIsAMiss() = runTest {
        val cache = ShowtimesCache(FakeClock())
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        assertThat(cache.get(venue, day.plus(1, DateTimeUnit.DAY))).isNull()
        assertThat(cache.get(venue(2), day)).isNull()
    }

    @Test
    fun anEntryOlderThanTheTtlIsDropped() = runTest {
        val cache = ShowtimesCache(FakeClock(), ttlMillis = -1)
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        assertThat(cache.get(venue, day)).isNull()
    }

    @Test
    fun writesFromConcurrentFetchesAreAllKept() = runTest {
        val cache = ShowtimesCache(FakeClock())

        (1..200).map { index ->
            async(Dispatchers.Default) { cache.put(venue(index), day, listOf(showing(venue(index)))) }
        }.awaitAll()

        (1..200).forEach { index -> assertThat(cache.get(venue(index), day)?.size).isEqualTo(1) }
    }

    @Test
    fun clearEmptiesTheCache() = runTest {
        val cache = ShowtimesCache(FakeClock())
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        cache.clear()

        assertThat(cache.get(venue, day)).isNull()
    }

    @Test
    fun anEntryExactlyAsOldAsTheTtlIsStillRead() = runTest {
        val clock = FakeClock()
        val cache = ShowtimesCache(clock, ttlMillis = 1_000)
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        clock.millis = 1_000

        assertThat(cache.get(venue, day)).isEqualTo(listOf(showing(venue)))
    }

    @Test
    fun anEntryOneMillisecondPastTheTtlIsDropped() = runTest {
        val clock = FakeClock()
        val cache = ShowtimesCache(clock, ttlMillis = 1_000)
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        clock.millis = 1_001

        assertThat(cache.get(venue, day)).isNull()
    }

    @Test
    fun aClockThatGoesBackKeepsTheEntry() = runTest {
        val clock = FakeClock(millis = 10_000)
        val cache = ShowtimesCache(clock, ttlMillis = 1_000)
        val venue = venue(1)
        cache.put(venue, day, listOf(showing(venue)))

        clock.millis = 0

        assertThat(cache.get(venue, day)).isEqualTo(listOf(showing(venue)))
    }
}

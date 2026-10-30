package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MultiCinemaShowingsFailureTest {

    private class FakeDataSource(
        override val chain: Chain,
        private val failingDays: Set<LocalDate> = emptySet(),
        private val failAll: Boolean = false,
    ) : ShowtimesDataSource {
        override suspend fun getVenues(): Result<List<Venue>, DataError.Network> = Result.Success(emptyList())

        override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> {
            if (failAll || days.any { it in failingDays }) return Result.Failure(DataError.Network.SERVER_ERROR)
            return Result.Success(days.map { Showing("Film", it, LocalTime(20, 30), venue) })
        }

        override suspend fun getFilmDetails(detailsUrl: String): Result<FilmDetails, DataError.Network> =
            Result.Failure(DataError.Network.NOT_FOUND)
    }

    private val day1 = LocalDate(2026, 11, 1)
    private val day2 = LocalDate(2026, 11, 2)
    private val venue = Venue(Chain.UCI, "uci-1", "UCI Test")

    @Test
    fun failingVenueWithNothingCachedSurfacesAsFailure() = runTest {
        val repository = MultiCinemaShowtimesRepository(listOf(FakeDataSource(Chain.UCI, failAll = true)))

        val result = repository.getShowings(listOf(day1), setOf(venue)).single()

        assertIs<Result.Failure<DataError.Network>>(result.result)
        assertEquals(DataError.Network.SERVER_ERROR, (result.result as Result.Failure).error)
    }

    @Test
    fun partialFailureKeepsCachedShowingsAsSuccess() = runTest {
        val cache = ShowtimesCache()
        MultiCinemaShowtimesRepository(listOf(FakeDataSource(Chain.UCI)), cache).getShowings(listOf(day1), setOf(venue))

        val failingSource = FakeDataSource(Chain.UCI, failingDays = setOf(day2))
        val result = MultiCinemaShowtimesRepository(listOf(failingSource), cache)
            .getShowings(listOf(day1, day2), setOf(venue))
            .single()

        assertIs<Result.Success<List<Showing>>>(result.result)
        assertEquals(listOf(day1), (result.result as Result.Success).data.map { it.day })
    }
}

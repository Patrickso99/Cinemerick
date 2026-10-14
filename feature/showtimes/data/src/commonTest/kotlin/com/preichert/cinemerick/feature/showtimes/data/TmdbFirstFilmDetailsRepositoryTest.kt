package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmLookup
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TmdbFirstFilmDetailsRepositoryTest {

    private val tmdb = FilmDetails(synopsis = "TMDB plot", runtimeMinutes = 100)
    private val known = FilmDetails(synopsis = "UCI plot", genre = "Horror")
    private val sources = listOf(DetailsSource(Chain.NOTORIOUS, "https://n/1"))
    private val cinemaResult = FilmDetails(synopsis = "Page plot", genre = "Horror", director = "Rob Savage")

    private class FakeLookup(private val outcomes: MutableList<Result<FilmDetails?, DataError.Network>>) : FilmLookup {
        val searched = mutableListOf<String>()
        override suspend fun find(searchTitle: String): Result<FilmDetails?, DataError.Network> {
            searched += searchTitle
            return outcomes.removeFirst()
        }
    }

    private inner class FakeShowtimes : ShowtimesRepository {
        val calls = mutableListOf<Pair<FilmDetails, List<DetailsSource>>>()
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = emptyMap()
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails {
            calls += known to sources
            return cinemaResult
        }
    }

    private fun found(details: FilmDetails?) = Result.Success(details)
    private fun failed() = Result.Failure(DataError.Network.NO_INTERNET)

    private fun repository(vararg outcomes: Result<FilmDetails?, DataError.Network>): Triple<TmdbFirstFilmDetailsRepository, FakeLookup, FakeShowtimes> {
        val lookup = FakeLookup(outcomes.toMutableList())
        val showtimes = FakeShowtimes()
        return Triple(TmdbFirstFilmDetailsRepository(lookup, showtimes), lookup, showtimes)
    }

    @Test
    fun aMatchReturnsTheDatabaseDetailsAndNeverTouchesTheCinemaFallback() = runTest {
        val (repository, _, showtimes) = repository(found(tmdb))

        val details = repository.getFilmDetails("Mammina", known, sources)

        assertEquals(tmdb, details)
        assertEquals(emptyList(), showtimes.calls)
    }

    @Test
    fun noMatchFallsBackToTheCinemasWithTheSameKnownAndSources() = runTest {
        val (repository, _, showtimes) = repository(found(null))

        val details = repository.getFilmDetails("Mammina", known, sources)

        assertEquals(cinemaResult, details)
        assertEquals(listOf(known to sources), showtimes.calls)
    }

    @Test
    fun aFailedLookupFallsBackAndIsAskedAgainNextTime() = runTest {
        val (repository, lookup, showtimes) = repository(failed(), found(tmdb))

        val first = repository.getFilmDetails("Mammina", known, sources)
        val second = repository.getFilmDetails("Mammina", known, sources)

        assertEquals(cinemaResult, first)
        assertEquals(tmdb, second)
        assertEquals(2, lookup.searched.size)
        assertEquals(1, showtimes.calls.size)
    }

    @Test
    fun aRepeatedCallReusesTheRememberedMatch() = runTest {
        val (repository, lookup, _) = repository(found(tmdb))

        repository.getFilmDetails("Mammina", known, sources)
        val again = repository.getFilmDetails("Mammina", known, sources)

        assertEquals(tmdb, again)
        assertEquals(1, lookup.searched.size)
    }

    @Test
    fun titlesThatNormalizeToTheSameFilmShareOneLookup() = runTest {
        val (repository, lookup, _) = repository(found(tmdb))

        repository.getFilmDetails("Dune (3D)", known, sources)
        val other = repository.getFilmDetails("DUNE", known, sources)

        assertEquals(tmdb, other)
        assertEquals(listOf("Dune"), lookup.searched)
    }

    @Test
    fun aRememberedNoMatchSkipsTheDatabaseButStillReadsTheCinemas() = runTest {
        val (repository, lookup, showtimes) = repository(found(null))

        repository.getFilmDetails("Mammina", known, sources)
        val again = repository.getFilmDetails("Mammina", known, sources)

        assertEquals(cinemaResult, again)
        assertEquals(1, lookup.searched.size)
        assertEquals(2, showtimes.calls.size)
    }

    @Test
    fun theLookupGetsTheSearchTitleNotTheFilmKey() = runTest {
        val (repository, lookup, _) = repository(found(null))

        repository.getFilmDetails("Café - Lingua originale", known, sources)

        assertEquals(listOf("Café"), lookup.searched)
    }

    @Test
    fun aMatchWithNothingInItCountsAsNoMatch() = runTest {
        val (repository, _, showtimes) = repository(found(FilmDetails()))

        val details = repository.getFilmDetails("Mammina", known, sources)

        assertEquals(cinemaResult, details)
        assertEquals(1, showtimes.calls.size)
    }
}

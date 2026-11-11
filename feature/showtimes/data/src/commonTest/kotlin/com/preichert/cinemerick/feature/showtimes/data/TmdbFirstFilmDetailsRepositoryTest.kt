package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo

class TmdbFirstFilmDetailsRepositoryTest {

    private val tmdb = FilmDetails(synopsis = "TMDB plot", runtimeMinutes = 100)
    private val known = FilmDetails(synopsis = "UCI plot", genre = "Horror")
    private val sources = listOf(DetailsSource(Chain.NOTORIOUS, "https://n/1"))
    private val cinemaResult = FilmDetails(synopsis = "Page plot", genre = "Horror", director = "Rob Savage")

    private class FakeFilmDataSource(private val outcomes: MutableList<Result<FilmDetails?, DataError.Network>>) : FilmRemoteDataSource {
        val searched = mutableListOf<String>()
        override suspend fun find(searchTitle: String): Result<FilmDetails?, DataError.Network> {
            searched += searchTitle
            return outcomes.removeFirst()
        }
        override suspend fun movieId(searchTitle: String): Result<Int?, DataError.Network> = Result.Success(null)
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

    private fun repository(vararg outcomes: Result<FilmDetails?, DataError.Network>): Triple<TmdbFirstFilmDetailsRepository, FakeFilmDataSource, FakeShowtimes> {
        val lookup = FakeFilmDataSource(outcomes.toMutableList())
        val showtimes = FakeShowtimes()
        return Triple(TmdbFirstFilmDetailsRepository(lookup, showtimes), lookup, showtimes)
    }

    @Test
    fun aMatchReturnsTheDatabaseDetailsAndNeverTouchesTheCinemaFallback() = runTest {
        val (repository, _, showtimes) = repository(found(tmdb))

        val details = repository.getFilmDetails("Mammina", known, sources)

        assertThat(details).isEqualTo(tmdb)
        assertThat(showtimes.calls).isEmpty()
    }

    @Test
    fun databaseDetailsAreAskedOncePerFilmAndNeverTouchTheCinemas() = runTest {
        val (repository, lookup, showtimes) = repository(found(tmdb))

        assertThat(repository.getDatabaseDetails("Mammina")).isEqualTo(found(tmdb))
        assertThat(repository.getDatabaseDetails("Mammina")).isEqualTo(found(tmdb))

        assertThat(lookup.searched.size).isEqualTo(1)
        assertThat(showtimes.calls).isEmpty()
    }

    @Test
    fun aConfidentNoMatchIsSuccessWithNoDetails() = runTest {
        val (repository, _, _) = repository(found(null))

        assertThat(repository.getDatabaseDetails("Mammina")).isEqualTo(found(null))
    }

    @Test
    fun aFailedLookupIsAFailureSoTheCallerDoesNotRememberIt() = runTest {
        val (repository, _, _) = repository(failed())

        assertThat(repository.getDatabaseDetails("Mammina")).isEqualTo(failed())
    }

    @Test
    fun noMatchFallsBackToTheCinemasWithTheSameKnownAndSources() = runTest {
        val (repository, _, showtimes) = repository(found(null))

        val details = repository.getFilmDetails("Mammina", known, sources)

        assertThat(details).isEqualTo(cinemaResult)
        assertThat(showtimes.calls).isEqualTo(listOf(known to sources))
    }

    @Test
    fun aFailedLookupFallsBackAndIsAskedAgainNextTime() = runTest {
        val (repository, lookup, showtimes) = repository(failed(), found(tmdb))

        val first = repository.getFilmDetails("Mammina", known, sources)
        val second = repository.getFilmDetails("Mammina", known, sources)

        assertThat(first).isEqualTo(cinemaResult)
        assertThat(second).isEqualTo(tmdb)
        assertThat(lookup.searched.size).isEqualTo(2)
        assertThat(showtimes.calls.size).isEqualTo(1)
    }

    @Test
    fun aRepeatedCallReusesTheRememberedMatch() = runTest {
        val (repository, lookup, _) = repository(found(tmdb))

        repository.getFilmDetails("Mammina", known, sources)
        val again = repository.getFilmDetails("Mammina", known, sources)

        assertThat(again).isEqualTo(tmdb)
        assertThat(lookup.searched.size).isEqualTo(1)
    }

    @Test
    fun titlesThatNormalizeToTheSameFilmShareOneLookup() = runTest {
        val (repository, lookup, _) = repository(found(tmdb))

        repository.getFilmDetails("Dune (3D)", known, sources)
        val other = repository.getFilmDetails("DUNE", known, sources)

        assertThat(other).isEqualTo(tmdb)
        assertThat(lookup.searched).isEqualTo(listOf("Dune"))
    }

    @Test
    fun aRememberedNoMatchSkipsTheDatabaseButStillReadsTheCinemas() = runTest {
        val (repository, lookup, showtimes) = repository(found(null))

        repository.getFilmDetails("Mammina", known, sources)
        val again = repository.getFilmDetails("Mammina", known, sources)

        assertThat(again).isEqualTo(cinemaResult)
        assertThat(lookup.searched.size).isEqualTo(1)
        assertThat(showtimes.calls.size).isEqualTo(2)
    }

    @Test
    fun theLookupGetsTheSearchTitleNotTheFilmKey() = runTest {
        val (repository, lookup, _) = repository(found(null))

        repository.getFilmDetails("Café - Lingua originale", known, sources)

        assertThat(lookup.searched).isEqualTo(listOf("Café"))
    }

    @Test
    fun aMatchWithNothingInItCountsAsNoMatch() = runTest {
        val (repository, _, showtimes) = repository(found(FilmDetails()))

        val details = repository.getFilmDetails("Mammina", known, sources)

        assertThat(details).isEqualTo(cinemaResult)
        assertThat(showtimes.calls.size).isEqualTo(1)
    }
}

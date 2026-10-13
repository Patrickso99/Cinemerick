package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiCinemaFilmDetailsTest {

    private class FakeDataSource(
        override val chain: Chain,
        private val pages: Map<String, Result<FilmDetails, DataError.Network>>
    ) : ShowtimesDataSource {
        val requested = mutableListOf<String>()

        override suspend fun getVenues(): Result<List<Venue>, DataError.Network> = Result.Success(emptyList())

        override suspend fun getShowings(venue: Venue, days: List<LocalDate>): Result<List<Showing>, DataError.Network> =
            Result.Success(emptyList())

        override suspend fun getFilmDetails(detailsUrl: String): Result<FilmDetails, DataError.Network> {
            requested += detailsUrl
            return pages[detailsUrl] ?: Result.Failure(DataError.Network.NOT_FOUND)
        }
    }

    private val notoriousPage = FilmDetails(synopsis = "Notorious plot", genre = "Drama", trailerUrl = "https://youtu.be/n")
    private val cristalloPage = FilmDetails(synopsis = "Cristallo plot", director = "Dir")

    private val notorious = FakeDataSource(Chain.NOTORIOUS, mapOf("n1" to Result.Success(notoriousPage)))
    private val cristallo = FakeDataSource(Chain.CRISTALLO, mapOf("c1" to Result.Success(cristalloPage)))
    private val repository = MultiCinemaShowtimesRepository(listOf(notorious, cristallo))

    private val sources = listOf(DetailsSource(Chain.NOTORIOUS, "n1"), DetailsSource(Chain.CRISTALLO, "c1"))

    @Test
    fun fetchesPagesAndMergesThemFieldByFieldBestChainFirst() = runTest {
        val details = repository.getFilmDetails(FilmDetails(rating = "14+"), sources)

        assertEquals(
            FilmDetails(synopsis = "Notorious plot", genre = "Drama", director = "Dir", rating = "14+", trailerUrl = "https://youtu.be/n"),
            details
        )
    }

    @Test
    fun knownFieldsAreNeverOverwritten() = runTest {
        val details = repository.getFilmDetails(FilmDetails(synopsis = "UCI plot"), sources)

        assertEquals("UCI plot", details.synopsis)
    }

    @Test
    fun noRequestIsMadeWhenNothingIsMissing() = runTest {
        val complete = FilmDetails("s", "g", "d", "c", "r", "t")

        assertEquals(complete, repository.getFilmDetails(complete, sources))
        assertEquals(emptyList(), notorious.requested + cristallo.requested)
    }

    @Test
    fun noRequestIsMadeWithoutSources() = runTest {
        val known = FilmDetails(synopsis = "UCI plot")

        assertEquals(known, repository.getFilmDetails(known, emptyList()))
    }

    @Test
    fun reopeningReusesThePagesAlreadyFetched() = runTest {
        repository.getFilmDetails(FilmDetails(), sources)
        repository.getFilmDetails(FilmDetails(), sources)

        assertEquals(listOf("n1"), notorious.requested)
        assertEquals(listOf("c1"), cristallo.requested)
    }

    @Test
    fun aFailedPageKeepsTheOthersAndTheKnownDetails() = runTest {
        val broken = MultiCinemaShowtimesRepository(
            listOf(FakeDataSource(Chain.NOTORIOUS, emptyMap()), cristallo)
        )

        val details = broken.getFilmDetails(FilmDetails(genre = "Horror"), sources)

        assertEquals(FilmDetails(synopsis = "Cristallo plot", genre = "Horror", director = "Dir"), details)
    }

    @Test
    fun aFailedPageIsRetriedNextTime() = runTest {
        val flaky = FakeDataSource(Chain.NOTORIOUS, emptyMap())
        val repository = MultiCinemaShowtimesRepository(listOf(flaky))

        repository.getFilmDetails(FilmDetails(), listOf(DetailsSource(Chain.NOTORIOUS, "n1")))
        repository.getFilmDetails(FilmDetails(), listOf(DetailsSource(Chain.NOTORIOUS, "n1")))

        assertEquals(listOf("n1", "n1"), flaky.requested)
    }

    @Test
    fun aChainWithoutDataSourceContributesNothing() = runTest {
        val details = repository.getFilmDetails(FilmDetails(genre = "Horror"), listOf(DetailsSource(Chain.ARCADIA, "a1")))

        assertEquals(FilmDetails(genre = "Horror"), details)
    }
}

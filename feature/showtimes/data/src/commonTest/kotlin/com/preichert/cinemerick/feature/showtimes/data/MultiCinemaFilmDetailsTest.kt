package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.testing.FakeClock
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo

class MultiCinemaFilmDetailsTest {

    private class FakeDataSource(
        override val chain: Chain,
        private val pages: Map<String, Result<FilmDetails, DataError.Network>>
    ) : ShowtimesRemoteDataSource {
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
    private val repository = MultiCinemaShowtimesRepository(listOf(notorious, cristallo), ShowtimesCache(FakeClock()))

    private val sources = listOf(DetailsSource(Chain.NOTORIOUS, "n1"), DetailsSource(Chain.CRISTALLO, "c1"))

    @Test
    fun fetchesPagesAndMergesThemFieldByFieldBestChainFirst() = runTest {
        val details = repository.getFilmDetails(FilmDetails(rating = "14+"), sources)

        assertThat(details).isEqualTo(FilmDetails(synopsis = "Notorious plot", genre = "Drama", director = "Dir", rating = "14+", trailerUrl = "https://youtu.be/n"))
    }

    @Test
    fun knownFieldsAreNeverOverwritten() = runTest {
        val details = repository.getFilmDetails(FilmDetails(synopsis = "UCI plot"), sources)

        assertThat(details.synopsis).isEqualTo("UCI plot")
    }

    @Test
    fun noRequestIsMadeWhenNothingIsMissing() = runTest {
        val complete = FilmDetails("s", "g", "d", "c", "r", "t")

        assertThat(repository.getFilmDetails(complete, sources)).isEqualTo(complete)
        assertThat(notorious.requested + cristallo.requested).isEmpty()
    }

    @Test
    fun noRequestIsMadeWithoutSources() = runTest {
        val known = FilmDetails(synopsis = "UCI plot")

        assertThat(repository.getFilmDetails(known, emptyList())).isEqualTo(known)
    }

    @Test
    fun reopeningReusesThePagesAlreadyFetched() = runTest {
        repository.getFilmDetails(FilmDetails(), sources)
        repository.getFilmDetails(FilmDetails(), sources)

        assertThat(notorious.requested).isEqualTo(listOf("n1"))
        assertThat(cristallo.requested).isEqualTo(listOf("c1"))
    }

    @Test
    fun aFailedPageKeepsTheOthersAndTheKnownDetails() = runTest {
        val broken = MultiCinemaShowtimesRepository(
            listOf(FakeDataSource(Chain.NOTORIOUS, emptyMap()), cristallo),
            ShowtimesCache(FakeClock())
        )

        val details = broken.getFilmDetails(FilmDetails(genre = "Horror"), sources)

        assertThat(details).isEqualTo(FilmDetails(synopsis = "Cristallo plot", genre = "Horror", director = "Dir"))
    }

    @Test
    fun aFailedPageIsRetriedNextTime() = runTest {
        val flaky = FakeDataSource(Chain.NOTORIOUS, emptyMap())
        val repository = MultiCinemaShowtimesRepository(listOf(flaky), ShowtimesCache(FakeClock()))

        repository.getFilmDetails(FilmDetails(), listOf(DetailsSource(Chain.NOTORIOUS, "n1")))
        repository.getFilmDetails(FilmDetails(), listOf(DetailsSource(Chain.NOTORIOUS, "n1")))

        assertThat(flaky.requested).isEqualTo(listOf("n1", "n1"))
    }

    @Test
    fun aChainWithoutDataSourceContributesNothing() = runTest {
        val details = repository.getFilmDetails(FilmDetails(genre = "Horror"), listOf(DetailsSource(Chain.ARCADIA, "a1")))

        assertThat(details).isEqualTo(FilmDetails(genre = "Horror"))
    }
}

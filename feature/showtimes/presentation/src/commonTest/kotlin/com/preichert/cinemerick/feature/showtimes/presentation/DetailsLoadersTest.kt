package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

class DetailsLoadersTest {

    private val group = FilmGroupUi(
        title = "Dune",
        showings = emptyList(),
        details = FilmDetails(synopsis = "Cinema plot"),
        detailsSources = listOf(DetailsSource(com.preichert.cinemerick.feature.showtimes.domain.Chain.UCI, "https://uci/dune")),
    )
    private val person = FilmPerson(id = 7, name = "Zendaya", role = "Chani")

    private val filmRepository = object : FilmDetailsRepository {
        override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>) =
            FilmDetails(synopsis = "Database plot")
        override suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network> = Result.Success(null)
        override suspend fun getMovieId(title: String): Result<Int?, DataError.Network> = Result.Success(null)
    }

    @Test
    fun aFilmOverlayResolvesToTheRepositoryDetails() = runTest {
        val state = MutableStateFlow(ShowtimesState())
        val loader = FilmDetailsLoader(this, filmRepository) { change -> state.update { change(it) } }

        loader.open(group)
        assertThat(state.value.filmDetails?.isLoading).isEqualTo(true)
        advanceUntilIdle()

        assertThat(state.value.filmDetails?.isLoading).isEqualTo(false)
        assertThat(state.value.filmDetails?.details?.synopsis).isEqualTo("Database plot")
    }

    @Test
    fun dismissingTheFilmOverlayClosesIt() = runTest {
        val state = MutableStateFlow(ShowtimesState())
        val loader = FilmDetailsLoader(this, filmRepository) { change -> state.update { change(it) } }
        loader.open(group)
        advanceUntilIdle()

        loader.dismiss()

        assertThat(state.value.filmDetails).isNull()
    }

    @Test
    fun aFailedPersonLookupKeepsTheNameAndNoDetails() = runTest {
        val lookup = object : PersonRemoteDataSource {
            override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Failure(DataError.Network.NO_INTERNET)
        }
        val state = MutableStateFlow(ShowtimesState())
        val loader = PersonDetailsLoader(this, lookup) { change -> state.update { change(it) } }

        loader.open(person)
        advanceUntilIdle()

        assertThat(state.value.personDetails).isEqualTo(PersonUi(person, details = null, isLoading = false))
    }
}

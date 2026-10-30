package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        assertEquals(true, state.value.filmDetails?.isLoading)
        advanceUntilIdle()

        assertEquals(false, state.value.filmDetails?.isLoading)
        assertEquals("Database plot", state.value.filmDetails?.details?.synopsis)
    }

    @Test
    fun dismissingTheFilmOverlayClosesIt() = runTest {
        val state = MutableStateFlow(ShowtimesState())
        val loader = FilmDetailsLoader(this, filmRepository) { change -> state.update { change(it) } }
        loader.open(group)
        advanceUntilIdle()

        loader.dismiss()

        assertNull(state.value.filmDetails)
    }

    @Test
    fun aFailedPersonLookupKeepsTheNameAndNoDetails() = runTest {
        val lookup = object : PersonLookup {
            override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Failure(DataError.Network.NO_INTERNET)
        }
        val state = MutableStateFlow(ShowtimesState())
        val loader = PersonDetailsLoader(this, lookup) { change -> state.update { change(it) } }

        loader.open(person)
        advanceUntilIdle()

        assertEquals(PersonUi(person, details = null, isLoading = false), state.value.personDetails)
    }
}

package com.preichert.cinemerick.feature.showtimes.presentation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.test.AfterTest
import kotlin.test.Test
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences

/*
 * Break vectors for generating showtimes, one angle per test:
 * a second generate while the first is still in flight, and a time typed in an invalid shape.
 */
class ShowtimesGenerateTest {

    private val day = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
    private val venue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
    private val controller = TimeWindowController(FakeTimeWindowPreferences())
    private val scheduler = TestCoroutineScheduler()

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun aSecondGenerateWhileLoadingDropsTheEarlierAnswer() = runTest {
        val slow = CompletableDeferred<List<Showing>>()
        val repository = ScriptedRepository(venue) { call ->
            if (call == 1) slow.await() else listOf(showing("21:00"))
        }
        val viewModel = viewModel(repository)
        viewModel.onAction(ShowtimesAction.OnDayToggle(day))

        viewModel.onAction(ShowtimesAction.OnGenerateClick)
        viewModel.onAction(ShowtimesAction.OnGenerateClick)
        slow.complete(listOf(showing("20:00")))

        assertThat(repository.calls).isEqualTo(2)
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.groups.single().showings.map { it.time }).containsExactly("21:00")
    }

    @Test
    fun anInvalidTypedTimeNeverReachesTheRepository() = runTest {
        val repository = ScriptedRepository(venue) { listOf(showing("21:00")) }
        val viewModel = viewModel(repository)
        viewModel.onAction(ShowtimesAction.OnDayToggle(day))
        viewModel.onAction(ShowtimesAction.OnMinTimeChange(day, "1,5"))
        // Changing the day already triggers a silent fetch; the explicit click must not add another.
        val callsBeforeClick = repository.calls

        viewModel.onAction(ShowtimesAction.OnGenerateClick)

        assertThat(repository.calls).isEqualTo(callsBeforeClick)
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    private fun showing(time: String) = Showing("Mammina", day, LocalTime.parse(time), venue)

    private fun viewModel(repository: ShowtimesRepository): ShowtimesViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(scheduler))
        return ShowtimesViewModel(repository, FakeFilmDetailsRepository, selectedOnly(venue), controller, FakePersonLookup)
    }

    // The venue is selected from the start, so a toggle cannot remove it when it is a default.
    private fun selectedOnly(venue: Venue) = object : FilterPreferences {
        override fun getSelectedVenues(): Set<Venue>? = setOf(venue)
        override fun setSelectedVenues(venues: Set<Venue>) = Unit
    }

    /** Answers each getShowings call in turn; [answer] receives the 1-based call number. */
    private class ScriptedRepository(
        private val venue: Venue,
        private val answer: suspend (call: Int) -> List<Showing>,
    ) : ShowtimesRepository {
        var calls = 0
            private set

        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = emptyMap()

        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> {
            val call = ++calls
            return listOf(VenueShowings(venue, Result.Success(answer(call))))
        }

        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private object FakeFilmDetailsRepository : FilmDetailsRepository {
        override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
        override suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network> = Result.Success(null)
        override suspend fun getMovieId(title: String): Result<Int?, DataError.Network> = Result.Success(null)
    }

    private object FakePersonLookup : PersonLookup {
        override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Success(null)
    }

}

package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.data.SettingsTimeWindowPreferences
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.MemorySettings
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShowtimesViewModelTest {

    private val first = LocalDate(2026, 10, 1)
    private val second = LocalDate(2026, 10, 2)

    private val controller = TimeWindowController(SettingsTimeWindowPreferences(MemorySettings()))
    // Main's clock, so a test can move past the card flush delay with scheduler.advanceUntilIdle().
    private val scheduler = TestCoroutineScheduler()

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        repository: ShowtimesRepository = FakeRepository,
        filmDetails: FilmDetailsRepository = FakeFilmDetailsRepository
    ): ShowtimesViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(scheduler))
        return ShowtimesViewModel(repository, filmDetails, FakeFilterPreferences, controller, FakePersonLookup)
    }

    private fun ShowtimesViewModel.day(date: LocalDate) = state.value.days.first { it.date == date }

    @Test
    fun newDayUsesConfiguredWindow() = runTest {
        controller.setMin(18 * 60)
        controller.setMax(23 * 60)
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertEquals("18:00", viewModel.day(first).minTime)
        assertEquals("23:00", viewModel.day(first).maxTime)
    }

    @Test
    fun unsetMaxGivesBlankMax() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertEquals("20:00", viewModel.day(first).minTime)
        assertEquals("", viewModel.day(first).maxTime)
    }

    @Test
    fun changingWindowOnlyAffectsDaysSelectedAfterwards() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        controller.setMin(18 * 60)
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        assertEquals("20:00", viewModel.day(first).minTime)
        assertEquals("18:00", viewModel.day(second).minTime)
    }

    @Test
    fun confirmKeepsTimeRangeOfRetainedDays() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnMinTimeChange(first, "15:00"))

        controller.setMin(18 * 60)
        viewModel.onAction(ShowtimesAction.OnDatesConfirm(setOf(second, first)))

        assertEquals("15:00", viewModel.day(first).minTime)
        assertEquals("18:00", viewModel.day(second).minTime)
    }

    @Test
    fun confirmDropsRemovedDaysAndSortsByDate() = runTest {
        val third = LocalDate(2026, 10, 3)
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        viewModel.onAction(ShowtimesAction.OnDatesConfirm(setOf(third, second)))

        assertEquals(listOf(second, third), viewModel.state.value.days.map { it.date })
    }

    @Test
    fun confirmClosesCalendar() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)

        viewModel.onAction(ShowtimesAction.OnDatesConfirm(setOf(first)))

        assertNull(viewModel.state.value.calendar)
    }

    @Test
    fun dismissLeavesDaysUnchanged() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)

        viewModel.onAction(ShowtimesAction.OnCalendarDismiss)

        assertEquals(listOf(first), viewModel.state.value.days.map { it.date })
        assertNull(viewModel.state.value.calendar)
    }

    @Test
    fun chipToggleStillAppliesImmediately() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertEquals(listOf(second), viewModel.state.value.days.map { it.date })
    }

    @Test
    fun showingUiCarriesTicketUrl() = runTest {
        val venue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
        val day = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
        val showings = listOf(
            Showing("Mammina", day, LocalTime(21, 0), venue, ticketUrl = "https://ucicinemas.it/movies/mammina/acquista/1/2/3"),
            Showing("Mammina", day, LocalTime(21, 30), venue)
        )
        val viewModel = viewModel(object : ShowtimesRepository by FakeRepository {
            override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>) =
                listOf(VenueShowings(venue, Result.Success(showings)))
        })
        viewModel.onAction(ShowtimesAction.OnDayToggle(day))

        viewModel.onAction(ShowtimesAction.OnGenerateClick)

        val shown = viewModel.state.value.groups.single().showings
        assertEquals(listOf("21:00", "21:30"), shown.map { it.time })
        assertEquals(listOf<String?>("https://ucicinemas.it/movies/mammina/acquista/1/2/3", null), shown.map { it.ticketUrl })
    }

    @Test
    fun soldOutShowingsStayListedButLeaveThePoll() = runTest {
        val venue = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
        val day = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
        val showings = listOf(
            Showing("Mammina", day, LocalTime(20, 30), venue, soldOut = true),
            Showing("Mammina", day, LocalTime(21, 0), venue)
        )
        val viewModel = viewModel(object : ShowtimesRepository by FakeRepository {
            override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>) =
                listOf(VenueShowings(venue, Result.Success(showings)))
        })
        viewModel.onAction(ShowtimesAction.OnDayToggle(day))

        viewModel.onAction(ShowtimesAction.OnGenerateClick)

        val state = viewModel.state.value
        assertEquals(listOf(true, false), state.groups.single().showings.map { it.soldOut })
        assertEquals(1, state.optionsCount)
        assertEquals(1, state.pollText.lines().size)
    }

    @Test
    fun onlySoldOutShowingsGiveEmptyPoll() = runTest {
        val venue = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
        val day = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
        val viewModel = viewModel(object : ShowtimesRepository by FakeRepository {
            override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>) =
                listOf(VenueShowings(venue, Result.Success(listOf(Showing("Mammina", day, LocalTime(20, 30), venue, soldOut = true)))))
        })
        viewModel.onAction(ShowtimesAction.OnDayToggle(day))

        viewModel.onAction(ShowtimesAction.OnGenerateClick)

        val state = viewModel.state.value
        assertEquals(1, state.groups.single().showings.size)
        assertEquals(0, state.optionsCount)
        assertEquals("", state.pollText)
    }

    private val notoriousVenue = Venue(Chain.NOTORIOUS, "cagliari", "Cagliari", null)
    private val uciVenue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
    private val soon = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)

    // A generated result holding one film, "Mammina", with the given showings.
    private fun viewModelShowing(
        showings: List<Showing>,
        venues: Set<Venue>,
        onDatabaseDetails: suspend (String) -> FilmDetails? = { null },
        onFilmDetails: suspend (String, FilmDetails, List<DetailsSource>) -> FilmDetails = { _, known, _ -> known }
    ): ShowtimesViewModel {
        val viewModel = viewModel(
            object : ShowtimesRepository by FakeRepository {
                override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>) =
                    showings.groupBy { it.venue }.map { (venue, list) -> VenueShowings(venue, Result.Success(list)) }
            },
            object : FilmDetailsRepository {
                override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>) =
                    onFilmDetails(title, known, sources)
                override suspend fun getDatabaseDetails(title: String) = onDatabaseDetails(title)
                override suspend fun getMovieId(title: String): Int? = null
            }
        )
        venues.forEach { viewModel.onAction(ShowtimesAction.OnVenueToggle(it)) }
        viewModel.onAction(ShowtimesAction.OnDayToggle(soon))
        viewModel.onAction(ShowtimesAction.OnGenerateClick)
        return viewModel
    }

    @Test
    fun openingDetailsShowsOnlyALoadingStateUntilTheLookupFinishes() = runTest {
        val gate = CompletableDeferred<FilmDetails>()
        val inline = FilmDetails(synopsis = "UCI plot", genre = "Horror")
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue, details = inline)), emptySet()
        ) { _, _, _ -> gate.await() }

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))
        // The cinemas' text is not shown while the film database may still replace it.
        assertEquals(FilmDetailsUi("Mammina", FilmDetails(), isLoading = true), viewModel.state.value.filmDetails)

        val resolved = FilmDetails(synopsis = "TMDB plot", runtimeMinutes = 95)
        gate.complete(resolved)
        assertEquals(FilmDetailsUi("Mammina", resolved, isLoading = false), viewModel.state.value.filmDetails)
    }

    @Test
    fun theRepositoryIsAskedWithTheTitleAndWhatTheCinemasCarry() = runTest {
        val inline = FilmDetails(synopsis = "UCI plot")
        var asked: Triple<String, FilmDetails, List<DetailsSource>>? = null
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), notoriousVenue, details = inline, detailsUrl = "https://n/1")),
            setOf(notoriousVenue)
        ) { title, known, sources ->
            asked = Triple(title, known, sources)
            known
        }

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        assertEquals(Triple("Mammina", inline, listOf(DetailsSource(Chain.NOTORIOUS, "https://n/1"))), asked)
        assertEquals(FilmDetailsUi("Mammina", inline, isLoading = false), viewModel.state.value.filmDetails)
    }

    @Test
    fun dismissingDetailsClosesTheOverlayAndIgnoresALateResult() = runTest {
        val gate = CompletableDeferred<FilmDetails>()
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), notoriousVenue, detailsUrl = "https://n/1")),
            setOf(notoriousVenue)
        ) { _, _, _ -> gate.await() }
        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        viewModel.onAction(ShowtimesAction.OnFilmDetailsDismiss)
        gate.complete(FilmDetails(synopsis = "late"))

        assertNull(viewModel.state.value.filmDetails)
    }

    @Test
    fun reopeningCancelsTheEarlierLookup() = runTest {
        val gates = mutableListOf<CompletableDeferred<FilmDetails>>()
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), notoriousVenue)),
            setOf(notoriousVenue)
        ) { _, _, _ -> CompletableDeferred<FilmDetails>().also { gates += it }.await() }

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))
        viewModel.onAction(ShowtimesAction.OnFilmDetailsDismiss)
        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))
        gates.first().complete(FilmDetails(synopsis = "stale"))
        assertTrue(viewModel.state.value.filmDetails!!.isLoading)

        gates.last().complete(FilmDetails(synopsis = "fresh"))
        assertEquals(FilmDetailsUi("Mammina", FilmDetails(synopsis = "fresh"), isLoading = false), viewModel.state.value.filmDetails)
    }

    @Test
    fun aLookupWithNothingToShowStopsLoadingOnTheEmptyOverlay() = runTest {
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), notoriousVenue)),
            setOf(notoriousVenue)
        ) { _, _, _ -> FilmDetails() }

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        val shown = viewModel.state.value.filmDetails!!
        assertFalse(shown.isLoading)
        assertTrue(shown.details.isEmpty)
    }

    @Test
    fun openingDetailsOfAFilmWithNoDetailsShowsAnEmptyOverlay() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet())

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        assertEquals(FilmDetailsUi("Mammina", FilmDetails(), isLoading = false), viewModel.state.value.filmDetails)
    }

    @Test
    fun openingDetailsOfAnUnknownFilmDoesNothing() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet())

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Other"))

        assertNull(viewModel.state.value.filmDetails)
    }

    @Test
    fun cardAsksTmdbOnceAndShowsItsFactsOnTheFilm() = runTest {
        val tmdb = FilmDetails(year = 2026, runtimeMinutes = 95)
        val asked = mutableListOf<String>()
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet(),
            onDatabaseDetails = { title -> asked += title; tmdb }
        )

        viewModel.onAction(ShowtimesAction.OnFilmCardVisible("Mammina"))
        viewModel.onAction(ShowtimesAction.OnFilmCardVisible("Mammina"))
        scheduler.advanceUntilIdle()

        assertEquals(listOf("Mammina"), asked)
        assertEquals(tmdb, viewModel.state.value.groups.single().tmdb)
    }

    @Test
    fun cardPosterIsPendingUntilItsLookupAnswers() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet())
        assertTrue(viewModel.state.value.groups.single().posterPending)

        viewModel.onAction(ShowtimesAction.OnFilmCardVisible("Mammina"))
        scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.groups.single().posterPending)
    }

    private object FakeRepository : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = emptyMap()
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private object FakeFilmDetailsRepository : FilmDetailsRepository {
        override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
        override suspend fun getDatabaseDetails(title: String): FilmDetails? = null
        override suspend fun getMovieId(title: String): Int? = null
    }

    private object FakePersonLookup : PersonLookup {
        override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Success(null)
    }

    private object FakeFilterPreferences : FilterPreferences {
        override fun getSelectedVenues(): Set<Venue>? = null
        override fun setSelectedVenues(venues: Set<Venue>) = Unit
    }
}

package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.SavedStateHandle
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
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
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences

class ShowtimesViewModelTest {

    private val first = LocalDate(2026, 10, 1)
    private val second = LocalDate(2026, 10, 2)

    private val controller = TimeWindowController(FakeTimeWindowPreferences())
    // Main's clock, so a test can move past the card flush delay with scheduler.advanceUntilIdle().
    private val scheduler = TestCoroutineScheduler()

    @Test
    fun aFailedCardLookupIsAskedAgainWhenTheCardReturns() = runTest {
        var calls = 0
        val viewModel = viewModel(filmDetails = object : FilmDetailsRepository by FakeFilmDetailsRepository {
            override suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network> =
                if (calls++ == 0) Result.Failure(DataError.Network.NO_INTERNET) else Result.Success(FilmDetails(synopsis = "Plot"))
        })

        viewModel.onAction(ShowtimesAction.OnFilmCardVisible("Dune"))
        scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.resolvedCards).doesNotContain("Dune")

        viewModel.onAction(ShowtimesAction.OnFilmCardVisible("Dune"))
        scheduler.advanceUntilIdle()

        assertThat(calls).isEqualTo(2)
        assertThat(viewModel.state.value.resolvedCards).contains("Dune")
    }

    @Test
    fun copyingThePollSendsTheTextAndAConfirmation() = runTest {
        val venue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
        val day = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
        val viewModel = viewModel(object : ShowtimesRepository by FakeRepository {
            override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>) =
                listOf(VenueShowings(venue, Result.Success(listOf(Showing("Mammina", day, LocalTime(21, 0), venue)))))
        })
        viewModel.onAction(ShowtimesAction.OnDayToggle(day))
        viewModel.onAction(ShowtimesAction.OnGenerateClick)

        viewModel.events.test {
            viewModel.onAction(ShowtimesAction.OnCopyClick)

            assertThat(awaitItem()).isEqualTo(ShowtimesEvent.CopyToClipboard(viewModel.state.value.pollText))
            assertThat(awaitItem()).isInstanceOf<ShowtimesEvent.ShowSnackbar>()
        }
    }

    @Test
    fun copyingAnEmptyPollSendsNothing() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(ShowtimesAction.OnCopyClick)

            expectNoEvents()
        }
    }

    @Test
    fun theSummaryPanelOpensAndCloses() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnFilterSummaryToggle)
        assertThat(viewModel.state.value.isFilterSummaryExpanded).isTrue()

        viewModel.onAction(ShowtimesAction.OnFilterSummaryDismiss)
        assertThat(viewModel.state.value.isFilterSummaryExpanded).isFalse()
    }

    @Test
    fun aChainRowClosesThePanelOpensTheCardAndScrollsToIt() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnFilterSummaryToggle)

        viewModel.events.test {
            viewModel.onAction(ShowtimesAction.OnFilterSummaryTargetClick(FilterTarget.ChainCard(Chain.NOTORIOUS)))

            assertThat(awaitItem()).isEqualTo(ShowtimesEvent.ScrollToFilter(FilterTarget.ChainCard(Chain.NOTORIOUS)))
        }
        assertThat(viewModel.state.value.isFilterSummaryExpanded).isFalse()
        assertThat(viewModel.state.value.expandedChains).contains(Chain.NOTORIOUS)
    }

    @Test
    fun anotherRowScrollsWithoutOpeningAChain() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(ShowtimesAction.OnFilterSummaryTargetClick(FilterTarget.TimeWindow(first)))

            assertThat(awaitItem()).isEqualTo(ShowtimesEvent.ScrollToFilter(FilterTarget.TimeWindow(first)))
        }
        assertThat(viewModel.state.value.expandedChains.isEmpty()).isTrue()
    }

    @Test
    fun editingTheFilmFilterAfterASearchMarksTheSummaryStaleUntilUpdate() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), setOf(uciVenue))
        assertThat(viewModel.state.value.toFilterSummary().isStale).isFalse()

        viewModel.onAction(ShowtimesAction.OnFilmFilterChange("Dune"))
        assertThat(viewModel.state.value.toFilterSummary().isStale).isTrue()

        viewModel.onAction(ShowtimesAction.OnFilterSummaryToggle)
        viewModel.onAction(ShowtimesAction.OnFilterSummaryUpdateClick)

        assertThat(viewModel.state.value.isFilterSummaryExpanded).isFalse()
        assertThat(viewModel.state.value.searchedFilmFilter).isEqualTo("Dune")
        assertThat(viewModel.state.value.toFilterSummary().isStale).isFalse()
    }

    @Test
    fun clearingForgetsTheSearchedFilmFilter() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), setOf(uciVenue))

        viewModel.onAction(ShowtimesAction.OnClearClick)

        assertThat(viewModel.state.value.searchedFilmFilter).isNull()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        repository: ShowtimesRepository = FakeRepository,
        filmDetails: FilmDetailsRepository = FakeFilmDetailsRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): ShowtimesViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(scheduler))
        return ShowtimesViewModel(repository, filmDetails, FakeFilterPreferences, controller, FakePersonDataSource, savedStateHandle = savedStateHandle)
    }

    private fun ShowtimesViewModel.day(date: LocalDate) = state.value.days.first { it.date == date }

    @Test
    fun filtersSurviveAProcessDeath() = runTest {
        val savedStateHandle = SavedStateHandle()
        viewModel(savedStateHandle = savedStateHandle).onAction(ShowtimesAction.OnFilmFilterChange("Dune"))

        // A new instance, as after the process is restored, starts from the saved filters.
        assertThat(viewModel(savedStateHandle = savedStateHandle).state.value.filmFilter).isEqualTo("Dune")
    }

    @Test
    fun anUnreadableSavedStateStartsFromTheDefaults() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("filters" to "not json"))

        assertThat(viewModel(savedStateHandle = savedStateHandle).state.value.filmFilter).isEqualTo("")
    }

    @Test
    fun newDayUsesConfiguredWindow() = runTest {
        controller.setMin(18 * 60)
        controller.setMax(23 * 60)
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertThat(viewModel.day(first).minTime).isEqualTo("18:00")
        assertThat(viewModel.day(first).maxTime).isEqualTo("23:00")
    }

    @Test
    fun unsetMaxGivesBlankMax() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertThat(viewModel.day(first).minTime).isEqualTo("20:00")
        assertThat(viewModel.day(first).maxTime).isEqualTo("")
    }

    @Test
    fun changingWindowOnlyAffectsDaysSelectedAfterwards() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        controller.setMin(18 * 60)
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        assertThat(viewModel.day(first).minTime).isEqualTo("20:00")
        assertThat(viewModel.day(second).minTime).isEqualTo("18:00")
    }

    // The calendar only takes days from today on, so these tests use days in the future.
    private val later = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(40, DateTimeUnit.DAY)
    private val later2 = later.plus(1, DateTimeUnit.DAY)
    private val later3 = later.plus(2, DateTimeUnit.DAY)

    @Test
    fun confirmKeepsTimeRangeOfRetainedDays() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(later))
        viewModel.onAction(ShowtimesAction.OnMinTimeChange(later, "15:00"))

        controller.setMin(18 * 60)
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)
        viewModel.onAction(ShowtimesAction.OnCalendarDayToggle(later2))
        viewModel.onAction(ShowtimesAction.OnDatesConfirm)

        assertThat(viewModel.day(later).minTime).isEqualTo("15:00")
        assertThat(viewModel.day(later2).minTime).isEqualTo("18:00")
    }

    @Test
    fun confirmDropsRemovedDaysAndSortsByDate() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(later))
        viewModel.onAction(ShowtimesAction.OnDayToggle(later2))

        viewModel.onAction(ShowtimesAction.OnCalendarOpen)
        viewModel.onAction(ShowtimesAction.OnCalendarDayToggle(later3))
        viewModel.onAction(ShowtimesAction.OnCalendarDayToggle(later))
        viewModel.onAction(ShowtimesAction.OnDatesConfirm)

        assertThat(viewModel.state.value.days.map { it.date }).isEqualTo(listOf(later2, later3))
    }

    @Test
    fun confirmClosesCalendar() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)
        viewModel.onAction(ShowtimesAction.OnCalendarDayToggle(later))

        viewModel.onAction(ShowtimesAction.OnDatesConfirm)

        assertThat(viewModel.state.value.calendar).isNull()
    }

    @Test
    fun dismissLeavesDaysUnchanged() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnCalendarOpen)

        viewModel.onAction(ShowtimesAction.OnCalendarDismiss)

        assertThat(viewModel.state.value.days.map { it.date }).isEqualTo(listOf(first))
        assertThat(viewModel.state.value.calendar).isNull()
    }

    @Test
    fun chipToggleStillAppliesImmediately() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(first))
        viewModel.onAction(ShowtimesAction.OnDayToggle(second))

        viewModel.onAction(ShowtimesAction.OnDayToggle(first))

        assertThat(viewModel.state.value.days.map { it.date }).isEqualTo(listOf(second))
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
        assertThat(shown.map { it.time }).isEqualTo(listOf("21:00", "21:30"))
        assertThat(shown.map { it.ticketUrl }).isEqualTo(listOf<String?>("https://ucicinemas.it/movies/mammina/acquista/1/2/3", null))
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
        assertThat(state.groups.single().showings.map { it.soldOut }).isEqualTo(listOf(true, false))
        assertThat(state.optionsCount).isEqualTo(1)
        assertThat(state.pollText.lines().size).isEqualTo(1)
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
        assertThat(state.groups.single().showings.size).isEqualTo(1)
        assertThat(state.optionsCount).isEqualTo(0)
        assertThat(state.pollText).isEqualTo("")
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
                override suspend fun getDatabaseDetails(title: String) = Result.Success(onDatabaseDetails(title))
                override suspend fun getMovieId(title: String): Result<Int?, DataError.Network> = Result.Success(null)
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
        assertThat(viewModel.state.value.filmDetails).isEqualTo(FilmDetailsUi("Mammina", FilmDetails(), isLoading = true))

        val resolved = FilmDetails(synopsis = "TMDB plot", runtimeMinutes = 95)
        gate.complete(resolved)
        assertThat(viewModel.state.value.filmDetails).isEqualTo(FilmDetailsUi("Mammina", resolved, isLoading = false))
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

        assertThat(asked).isEqualTo(Triple("Mammina", inline, listOf(DetailsSource(Chain.NOTORIOUS, "https://n/1"))))
        assertThat(viewModel.state.value.filmDetails).isEqualTo(FilmDetailsUi("Mammina", inline, isLoading = false))
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

        assertThat(viewModel.state.value.filmDetails).isNull()
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
        assertThat(viewModel.state.value.filmDetails!!.isLoading).isTrue()

        gates.last().complete(FilmDetails(synopsis = "fresh"))
        assertThat(viewModel.state.value.filmDetails).isEqualTo(FilmDetailsUi("Mammina", FilmDetails(synopsis = "fresh"), isLoading = false))
    }

    @Test
    fun aLookupWithNothingToShowStopsLoadingOnTheEmptyOverlay() = runTest {
        val viewModel = viewModelShowing(
            listOf(Showing("Mammina", soon, LocalTime(21, 0), notoriousVenue)),
            setOf(notoriousVenue)
        ) { _, _, _ -> FilmDetails() }

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        val shown = viewModel.state.value.filmDetails!!
        assertThat(shown.isLoading).isFalse()
        assertThat(shown.details.isEmpty).isTrue()
    }

    @Test
    fun openingDetailsOfAFilmWithNoDetailsShowsAnEmptyOverlay() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet())

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        assertThat(viewModel.state.value.filmDetails).isEqualTo(FilmDetailsUi("Mammina", FilmDetails(), isLoading = false))
    }

    @Test
    fun openingDetailsOfAnUnknownFilmDoesNothing() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet())

        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Other"))

        assertThat(viewModel.state.value.filmDetails).isNull()
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

        assertThat(asked).isEqualTo(listOf("Mammina"))
        assertThat(viewModel.state.value.groups.single().tmdb).isEqualTo(tmdb)
    }

    @Test
    fun cardPosterIsPendingUntilItsLookupAnswers() = runTest {
        val viewModel = viewModelShowing(listOf(Showing("Mammina", soon, LocalTime(21, 0), uciVenue)), emptySet())
        assertThat(viewModel.state.value.groups.single().posterPending).isTrue()

        viewModel.onAction(ShowtimesAction.OnFilmCardVisible("Mammina"))
        scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.groups.single().posterPending).isFalse()
    }

    private object FakeRepository : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = emptyMap()
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private object FakeFilmDetailsRepository : FilmDetailsRepository {
        override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
        override suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network> = Result.Success(null)
        override suspend fun getMovieId(title: String): Result<Int?, DataError.Network> = Result.Success(null)
    }

    private object FakePersonDataSource : PersonRemoteDataSource {
        override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Success(null)
    }

    private object FakeFilterPreferences : FilterPreferences {
        override fun getSelectedVenues(): Set<Venue>? = null
        override fun setSelectedVenues(venues: Set<Venue>) = Unit
    }
}

package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.Dispatchers
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock

/** The screen state that used to live in the composables: open chains, overlays' expansion, full-size images. */
class ShowtimesScreenStateTest {

    private val marcon = Venue(Chain.UCI, "uci-marcon", "UCI Luxe Marcon", "Veneto")
    private val soon = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(30, DateTimeUnit.DAY)
    private val image = FilmImage("https://example.com/t.jpg", "https://example.com/f.jpg")

    private val preferences = object : FilterPreferences {
        var saved: Set<Venue>? = null
        override fun getSelectedVenues(): Set<Venue>? = saved ?: emptySet()
        override fun setSelectedVenues(venues: Set<Venue>) {
            saved = venues
        }
    }

    private val repository = object : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> =
            mapOf(Chain.UCI to Result.Success(listOf(marcon)))
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> =
            listOf(VenueShowings(marcon, Result.Success(listOf(Showing("Mammina", soon, LocalTime(21, 0), marcon)))))
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private val filmDetails = object : FilmDetailsRepository {
        override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>) =
            FilmDetails(synopsis = "Plot")
        override suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network> = Result.Success(null)
        override suspend fun getMovieId(title: String): Result<Int?, DataError.Network> = Result.Success(null)
    }

    private val persons = object : PersonRemoteDataSource {
        override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Success(null)
    }

    private fun viewModel() = ShowtimesViewModel(
        repository, filmDetails, preferences, TimeWindowController(FakeTimeWindowPreferences()), persons,
        savedStateHandle = SavedStateHandle()
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun aChainCardOpensAndClosesAgain() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnChainExpandToggle(Chain.UCI))
        assertThat(viewModel.state.value.expandedChains).isEqualTo(setOf(Chain.UCI))

        viewModel.onAction(ShowtimesAction.OnChainExpandToggle(Chain.UCI))
        assertThat(viewModel.state.value.expandedChains).isEqualTo(emptySet())
    }

    @Test
    fun doneInTheVenuePickerSavesTheSelection() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnVenuePickerOpen(Chain.UCI))
        viewModel.onAction(ShowtimesAction.OnVenueDraftToggle(marcon))
        assertThat(preferences.saved).isNull()

        viewModel.onAction(ShowtimesAction.OnVenuePickerConfirm)

        assertThat(preferences.saved).isEqualTo(setOf(marcon))
        assertThat(viewModel.state.value.selectedVenues).isEqualTo(setOf(marcon))
    }

    @Test
    fun theSynopsisExpandsAndAReopenedFilmStartsCollapsed() = runTest {
        val viewModel = generated()
        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        viewModel.onAction(ShowtimesAction.OnSynopsisExpand)
        assertThat(viewModel.state.value.filmDetails?.isSynopsisExpanded).isEqualTo(true)

        viewModel.onAction(ShowtimesAction.OnFilmDetailsDismiss)
        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))
        assertThat(viewModel.state.value.filmDetails?.isSynopsisExpanded).isEqualTo(false)
    }

    @Test
    fun aGalleryImageOpensAndClosesInsideTheFilmDetails() = runTest {
        val viewModel = generated()
        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))

        viewModel.onAction(ShowtimesAction.OnFilmImageOpen(image))
        assertThat(viewModel.state.value.filmDetails?.openedImage).isEqualTo(image)

        viewModel.onAction(ShowtimesAction.OnFilmImageDismiss)
        assertThat(viewModel.state.value.filmDetails?.openedImage).isNull()
    }

    @Test
    fun closingTheFilmDetailsAlsoClosesItsImage() = runTest {
        val viewModel = generated()
        viewModel.onAction(ShowtimesAction.OnFilmDetailsOpen("Mammina"))
        viewModel.onAction(ShowtimesAction.OnFilmImageOpen(image))

        viewModel.onAction(ShowtimesAction.OnFilmDetailsDismiss)

        assertThat(viewModel.state.value.filmDetails).isNull()
    }

    @Test
    fun overlayActionsWithoutAnOpenOverlayAreIgnored() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnSynopsisExpand)
        viewModel.onAction(ShowtimesAction.OnFilmImageOpen(image))
        viewModel.onAction(ShowtimesAction.OnBiographyExpand)

        assertThat(viewModel.state.value.filmDetails).isNull()
        assertThat(viewModel.state.value.personDetails).isNull()
    }

    @Test
    fun theBiographyExpandsAndAnotherPersonStartsCollapsed() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnPersonOpen(FilmPerson(1, "Rob Savage")))

        viewModel.onAction(ShowtimesAction.OnBiographyExpand)
        assertThat(viewModel.state.value.personDetails?.isBiographyExpanded).isEqualTo(true)

        viewModel.onAction(ShowtimesAction.OnPersonOpen(FilmPerson(2, "Jessica Chastain")))
        assertThat(viewModel.state.value.personDetails?.isBiographyExpanded).isEqualTo(false)
    }

    @Test
    fun aPosterIsShownFullSizeUntilDismissed() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnPosterEnlarge("Mammina", "https://example.com/p.jpg"))
        assertThat(viewModel.state.value.enlargedPoster).isEqualTo(PosterUi("Mammina", "https://example.com/p.jpg"))

        viewModel.onAction(ShowtimesAction.OnPosterDismiss)
        assertThat(viewModel.state.value.enlargedPoster).isNull()
    }

    @Test
    fun clearingTheFiltersAlsoClosesTheChainCards() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnChainExpandToggle(Chain.UCI))

        viewModel.onAction(ShowtimesAction.OnClearClick)

        assertThat(viewModel.state.value.expandedChains.isEmpty()).isTrue()
        assertThat(viewModel.state.value.canClear).isFalse()
    }

    // A generated result holding one film, "Mammina", at UCI Luxe Marcon.
    private fun generated(): ShowtimesViewModel {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnVenueToggle(marcon))
        viewModel.onAction(ShowtimesAction.OnDayToggle(soon))
        viewModel.onAction(ShowtimesAction.OnGenerateClick)
        return viewModel
    }
}

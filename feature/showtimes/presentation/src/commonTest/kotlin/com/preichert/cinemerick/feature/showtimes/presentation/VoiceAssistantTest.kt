package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.SpeechError
import com.preichert.cinemerick.core.domain.assistant.SystemAiAvailability
import com.preichert.cinemerick.core.domain.assistant.SystemAiState
import com.preichert.cinemerick.core.domain.assistant.SpeechEvent
import com.preichert.cinemerick.core.presentation.LanguageController
import com.preichert.cinemerick.core.presentation.MicPermissionStatus
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.testing.FakeAssistantRepository
import com.preichert.cinemerick.core.testing.FakeLanguagePreferences
import com.preichert.cinemerick.core.testing.FakeTimeWindowPreferences
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequest
import com.preichert.cinemerick.feature.showtimes.domain.DayRequest
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.TimeSpan
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueMention
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class VoiceAssistantTest {

    private val silea = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
    private val marcon = Venue(Chain.UCI, "uci-marcon", "UCI Luxe Marcon", "Veneto")
    private val fiume = Venue(Chain.NOTORIOUS, "fiumeveneto", "Fiume Veneto", "Friuli")
    private val villesse = Venue(Chain.UCI, "uci-villesse", "UCI Villesse", "Friuli")

    // Wednesday 11 November 2026, 10:00 in UTC.
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-11T10:00:00Z")
    }
    private val thursday = LocalDate(2026, 11, 12)
    private val friday = LocalDate(2026, 11, 13)

    private val repository = object : ShowtimesRepository {
        override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = mapOf(
            Chain.THE_SPACE to Result.Success(listOf(silea)),
            Chain.UCI to Result.Success(listOf(marcon, villesse)),
            Chain.NOTORIOUS to Result.Failure(DataError.Network.NO_INTERNET),
        )
        override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = emptyList()
        override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails = known
    }

    private val filmDetails = object : FilmDetailsRepository {
        override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>) = known
        override suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network> = Result.Success(null)
        override suspend fun getMovieId(title: String): Result<Int?, DataError.Network> = Result.Success(null)
    }

    private val persons = object : PersonRemoteDataSource {
        override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> = Result.Success(null)
    }

    private class RecordingPreferences(var venues: Set<Venue>?) : FilterPreferences {
        override fun getSelectedVenues(): Set<Venue>? = venues
        override fun setSelectedVenues(venues: Set<Venue>) { this.venues = venues }
    }

    private val preferences = RecordingPreferences(setOf(fiume))
    private val speech = FakeSpeechRecognizer()
    private val interpreter = FakeCinemaRequestInterpreter()
    private val assistant = FakeAssistantRepository().apply { connect() }
    private val language = LanguageController(FakeLanguagePreferences(AppLanguage.ITALIANO))

    private fun viewModel() = ShowtimesViewModel(
        repository, filmDetails, preferences, TimeWindowController(FakeTimeWindowPreferences()), persons,
        speech, interpreter, assistant, language, clock = clock, savedStateHandle = SavedStateHandle(),
    )

    private fun ShowtimesViewModel.voice() = state.value.voice

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Opening ----------

    @Test
    fun withoutAnEngineTheSheetExplainsTheSetup() = runTest {
        assistant.removeKey()
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Setup)
    }

    @Test
    fun withoutSpeechTheSheetOpensForTyping() = runTest {
        speech.isAvailable = false
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.BLOCKED))

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Typing(""))
    }

    @Test
    fun microphoneNotGrantedYetShowsTheRationaleAndARefusalBlocksIt() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.NOT_GRANTED))
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.MicRationale)

        viewModel.onAction(ShowtimesAction.OnVoiceMicResult(MicPermissionStatus.NOT_GRANTED))
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.MicDenied(""))
    }

    @Test
    fun microphoneBlockedGoesStraightToTheDeniedSheet() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.BLOCKED))

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.MicDenied(""))
    }

    @Test
    fun listeningUsesTheAppLanguage() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))

        assertThat(speech.lastLanguageTag).isEqualTo("it")
    }

    // ---------- Listening ----------

    @Test
    fun partialTextIsShownWhileListening() = runTest {
        speech.keepOpen = true
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))

        speech.live.emit(SpeechEvent.Partial("UCI Marcon"))
        speech.live.emit(SpeechEvent.Level(0.5f))

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Listening("UCI Marcon", 0.5f))
    }

    @Test
    fun nothingSaidEndsInNothingHeardAndAPermissionErrorInDenied() = runTest {
        speech.events = listOf(SpeechEvent.Failed(SpeechError.NO_SPEECH))
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.NothingHeard)

        speech.events = listOf(SpeechEvent.Failed(SpeechError.PERMISSION))
        viewModel.onAction(ShowtimesAction.OnVoiceListen)
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.MicDenied(""))
    }

    @Test
    fun stoppingWithoutWordsIsNothingHeardAndWithWordsReadsThem() = runTest {
        speech.keepOpen = true
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))
        viewModel.onAction(ShowtimesAction.OnVoiceStopListening)
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.NothingHeard)
        assertThat(interpreter.calls).isEqualTo(0)

        viewModel.onAction(ShowtimesAction.OnVoiceListen)
        speech.live.emit(SpeechEvent.Partial("Hope domani"))
        viewModel.onAction(ShowtimesAction.OnVoiceStopListening)
        assertThat(interpreter.lastText).isEqualTo("Hope domani")
    }

    @Test
    fun typingKeepsTheTextHeardSoFar() = runTest {
        speech.keepOpen = true
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))
        speech.live.emit(SpeechEvent.Partial("Notorius Fiume"))

        viewModel.onAction(ShowtimesAction.OnVoiceType)

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Typing("Notorius Fiume"))
    }

    // ---------- Reading ----------

    @Test
    fun blankTypedRequestIsNotSentToTheAssistant() = runTest {
        speech.isAvailable = false
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.BLOCKED))
        viewModel.onAction(ShowtimesAction.OnVoiceTextChange("   \n "))

        viewModel.onAction(ShowtimesAction.OnVoiceInterpret)

        assertThat(viewModel.voice() is VoiceUi.NotUnderstood).isEqualTo(true)
        assertThat(interpreter.calls).isEqualTo(0)
    }

    @Test
    fun requestNamingNothingIsNotUnderstood() = runTest {
        interpreter.result = Result.Success(CinemaRequest())
        val viewModel = interpretTyped("Che tempo fa domani?")

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.NotUnderstood("Che tempo fa domani?"))
    }

    @Test
    fun keyRemovedMeanwhileShowsTheSetupAndOtherFailuresKeepTheText() = runTest {
        interpreter.result = Result.Failure(AssistantError.NO_ENGINE)
        assertThat(interpretTyped("Hope").voice()).isEqualTo(VoiceUi.Setup)

        interpreter.result = Result.Failure(AssistantError.INVALID_KEY)
        assertThat(interpretTyped("Hope").voice()).isEqualTo(VoiceUi.Failed("Hope", AssistantError.INVALID_KEY))
    }

    @Test
    fun secondExampleRequestBuildsTheReview() = runTest {
        interpreter.result = Result.Success(
            CinemaRequest(
                venues = listOf(
                    VenueMention("The Space Silea", Chain.THE_SPACE, "Silea"),
                    VenueMention("Uci Marcon", Chain.UCI, "Marcon"),
                    VenueMention("Notorius Fiume", Chain.NOTORIOUS, "Fiume"),
                ),
                days = listOf(DayRequest(thursday, TimeSpan("17:00", "20:00")), DayRequest(friday, TimeSpan("20:00", "24:00"))),
                films = listOf("Hope"),
            )
        )

        val review = (interpretTyped("…").voice() as VoiceUi.Review).review

        assertThat(review.venues).isEqualTo(listOf(silea, marcon))
        // Notorious did not load, so its selected venue stands in: "Notorius" was misspoken, so it is asked.
        assertThat(review.questions.map { it.best }).containsExactly(fiume)
        assertThat(review.days?.map { Triple(it.date, it.minTime, it.maxTime) }).isEqualTo(
            listOf(Triple(thursday, "17:00", "20:00"), Triple(friday, "20:00", "24:00"))
        )
        assertThat(review.formats).isNull()
        assertThat(review.films).isEqualTo(listOf("Hope"))
    }

    @Test
    fun windowWithoutDaysAppliesToTheSelectedDays() = runTest {
        interpreter.result = Result.Success(CinemaRequest(time = TimeSpan(from = "20:00")))
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnDayToggle(friday))

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.BLOCKED))
        viewModel.onAction(ShowtimesAction.OnVoiceTextChange("dopo le 20"))
        viewModel.onAction(ShowtimesAction.OnVoiceInterpret)

        val days = (viewModel.voice() as VoiceUi.Review).review.days.orEmpty()
        assertThat(days.map { it.date to it.minTime }).isEqualTo(listOf(friday to "20:00"))
    }

    @Test
    fun closingTheSheetWhileReadingIgnoresTheLateAnswer() = runTest {
        val answer = CompletableDeferred<Result<CinemaRequest, com.preichert.cinemerick.core.domain.assistant.AssistantError>>()
        interpreter.answer = answer
        val viewModel = interpretTyped("Hope")
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Interpreting("Hope"))

        viewModel.onAction(ShowtimesAction.OnVoiceDismiss)
        answer.complete(Result.Success(CinemaRequest(films = listOf("Hope"))))

        assertThat(viewModel.voice()).isNull()
    }

    // ---------- Review and search ----------

    @Test
    fun pickingACandidateAddsTheCinemaAndClosesTheQuestion() = runTest {
        interpreter.result = Result.Success(CinemaRequest(venues = listOf(VenueMention("Uci Villese", Chain.UCI, "Villese"))))
        val viewModel = interpretTyped("…")
        val question = (viewModel.voice() as VoiceUi.Review).review.questions.single()

        viewModel.onAction(ShowtimesAction.OnVoiceVenueCandidates(question.spoken))
        assertThat((viewModel.voice() as VoiceUi.Review).review.questions.single().showCandidates).isEqualTo(true)

        viewModel.onAction(ShowtimesAction.OnVoiceVenueConfirm(question.spoken, villesse))
        val review = (viewModel.voice() as VoiceUi.Review).review
        assertThat(review.venues).isEqualTo(listOf(villesse))
        assertThat(review.questions).isEqualTo(emptyList())
    }

    @Test
    fun searchReplacesOnlyTheMentionedGroupsAndOffersUndo() = runTest {
        interpreter.result = Result.Success(
            CinemaRequest(venues = listOf(VenueMention("UCI Marcon", Chain.UCI, "Marcon")), days = listOf(DayRequest(friday)))
        )
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnFilmFilterChange("Dune"))

        viewModel.events.test {
            typeAndRead(viewModel, "UCI Marcon venerdì")
            viewModel.onAction(ShowtimesAction.OnVoiceSearch)

            assertThat(awaitItem()).isEqualTo(ShowtimesEvent.ShowVoiceUndo)
            cancelAndIgnoreRemainingEvents()
        }
        val state = viewModel.state.value
        assertThat(state.voice).isNull()
        assertThat(state.selectedVenues).isEqualTo(setOf(marcon))
        assertThat(state.days.map { it.date }).containsExactly(friday)
        assertThat(state.filmFilter).isEqualTo("Dune")
        assertThat(preferences.venues).isEqualTo(setOf(marcon))
    }

    @Test
    fun cinemasMentionedButNoneFoundLeaveTheSelectionAlone() = runTest {
        interpreter.result = Result.Success(CinemaRequest(venues = listOf(VenueMention("Cinema Astra", null, "Astra")), films = listOf("Hope")))
        val viewModel = viewModel()

        typeAndRead(viewModel, "Hope al Cinema Astra")
        viewModel.onAction(ShowtimesAction.OnVoiceSearch)

        assertThat(viewModel.state.value.selectedVenues).isEqualTo(setOf(fiume))
        assertThat(viewModel.state.value.filmFilter).isEqualTo("Hope")
    }

    @Test
    fun onlyTwoDHidesTheOtherFormats() = runTest {
        interpreter.result = Result.Success(CinemaRequest(onlyFormats = setOf("2D")))
        val viewModel = viewModel()

        typeAndRead(viewModel, "solo 2D")
        viewModel.onAction(ShowtimesAction.OnVoiceSearch)

        assertThat(viewModel.state.value).prop(ShowtimesState::hiddenTags).isEqualTo(
            setOf("3D", "IMAX", "4DX", "SCREENX", "DOLBY ATMOS", "INFINITY VISION", "EPIC")
        )
    }

    @Test
    fun undoPutsTheFiltersBackOnceOnly() = runTest {
        interpreter.result = Result.Success(CinemaRequest(venues = listOf(VenueMention("UCI Marcon", Chain.UCI, "Marcon")), films = listOf("Hope")))
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnFilmFilterChange("Dune"))
        typeAndRead(viewModel, "Hope a UCI Marcon")
        viewModel.onAction(ShowtimesAction.OnVoiceSearch)

        viewModel.onAction(ShowtimesAction.OnVoiceUndo)
        assertThat(viewModel.state.value.selectedVenues).isEqualTo(setOf(fiume))
        assertThat(viewModel.state.value.filmFilter).isEqualTo("Dune")
        assertThat(preferences.venues).isEqualTo(setOf(fiume))
        assertThat(viewModel.state.value.hasGenerated).isEqualTo(false)

        // A second Undo (a double tap on the snackbar) changes nothing.
        viewModel.onAction(ShowtimesAction.OnFilmFilterChange("Wicked"))
        viewModel.onAction(ShowtimesAction.OnVoiceUndo)
        assertThat(viewModel.state.value.filmFilter).isEqualTo("Wicked")
    }

    @Test
    fun searchWithoutAReviewDoesNothing() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnVoiceSearch)

        assertThat(viewModel.state.value.selectedVenues).isEqualTo(setOf(fiume))
    }

    @Test
    fun theSheetNamesTheEngineThatAnswers() = runTest {
        val viewModel = viewModel()
        assertThat(viewModel.state.value.engineUi()).isEqualTo(EngineUi("Anthropic", onDevice = false))

        assistant.useSystemAi("Gemini Nano")
        assertThat(viewModel.state.value.engineUi()).isEqualTo(EngineUi("Gemini Nano", onDevice = true))

        assistant.removeKey()
        assertThat(viewModel.state.value.engineUi()).isNull()
    }

    @Test
    fun theSystemAiOfTheDeviceIsPassedToTheScreen() = runTest {
        val viewModel = viewModel()

        assistant.systemAi.value = SystemAiState("Apple Intelligence", SystemAiAvailability.NEEDS_ENABLING)

        assertThat(viewModel.state.value.systemAi).isEqualTo(SystemAiState("Apple Intelligence", SystemAiAvailability.NEEDS_ENABLING))
    }

    @Test
    fun theSystemAiIsAskedAtStartAndAgainEveryTimeTheAssistantIsOpened() = runTest {
        val viewModel = viewModel()
        assertThat(assistant.refreshes).isEqualTo(1)

        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))

        assertThat(assistant.refreshes).isEqualTo(2)
    }

    @Test
    fun theSetupSheetMovesOnToListeningOnceAnEngineIsReady() = runTest {
        assistant.removeKey()
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))
        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Setup)

        assistant.useSystemAi()

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Listening())
    }

    @Test
    fun theSetupSheetAsksForTheMicrophoneFirstWhenItWasNotGrantedYet() = runTest {
        assistant.removeKey()
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.NOT_GRANTED))

        assistant.useSystemAi()

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.MicRationale)
    }

    @Test
    fun anEngineBecomingReadyDoesNotDisturbWhatTheUserIsDoing() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.BLOCKED))
        viewModel.onAction(ShowtimesAction.OnVoiceTextChange("Hope"))

        assistant.useSystemAi()

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.MicDenied("Hope"))
    }

    @Test
    fun anEngineThatDisappearsWhileReadingChecksTheSystemAndShowsTheSetup() = runTest {
        interpreter.result = Result.Failure(AssistantError.NO_ENGINE)
        val before = assistant.refreshes

        val viewModel = interpretTyped("Hope")

        assertThat(viewModel.voice()).isEqualTo(VoiceUi.Setup)
        // At start, when the sheet opened, and when the engine answered that it was gone.
        assertThat(assistant.refreshes).isEqualTo(before + 3)
    }

    @Test
    fun downloadingTheSystemAiIsPassedToTheAssistant() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(ShowtimesAction.OnSystemAiDownload)

        assertThat(assistant.downloads).isEqualTo(1)
    }

    private fun interpretTyped(text: String): ShowtimesViewModel = viewModel().also { typeAndRead(it, text) }

    private fun typeAndRead(viewModel: ShowtimesViewModel, text: String) {
        viewModel.onAction(ShowtimesAction.OnAssistantClick(MicPermissionStatus.GRANTED))
        viewModel.onAction(ShowtimesAction.OnVoiceType)
        viewModel.onAction(ShowtimesAction.OnVoiceTextChange(text))
        viewModel.onAction(ShowtimesAction.OnVoiceInterpret)
    }
}

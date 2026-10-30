package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import androidx.lifecycle.viewModelScope
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.TimeWindowController
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.toTimeInput
import com.preichert.cinemerick.core.presentation.toUiText
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DayRange
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import com.preichert.cinemerick.feature.showtimes.domain.ParsedTime
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.availableTags
import com.preichert.cinemerick.feature.showtimes.domain.displayFormat
import com.preichert.cinemerick.feature.showtimes.domain.filterShowings
import com.preichert.cinemerick.feature.showtimes.domain.groupByFilm
import com.preichert.cinemerick.feature.showtimes.domain.needsFetch
import com.preichert.cinemerick.feature.showtimes.domain.parseTimeInput
import com.preichert.cinemerick.feature.showtimes.domain.toPollText
import com.preichert.cinemerick.feature.showtimes.domain.tomorrow
import com.preichert.cinemerick.feature.showtimes.domain.upcomingWeekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copied
import com.preichert.cinemerick.feature.showtimes.presentation.resources.invalid_time
import com.preichert.cinemerick.feature.showtimes.presentation.resources.select_day
import com.preichert.cinemerick.feature.showtimes.presentation.resources.venues_load_error
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val log = Logger.withTag("ShowtimesViewModel")

class ShowtimesViewModel(
    private val showtimesRepository: ShowtimesRepository,
    private val filmDetailsRepository: FilmDetailsRepository,
    private val filterPreferences: FilterPreferences,
    private val timeWindowController: TimeWindowController,
    private val personLookup: PersonLookup,
    private val clock: Clock = Clock.System
) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state = _state.asStateFlow()

    // Events are few and user-triggered, so they are queued until the screen collects them instead of being dropped.
    private val _events = Channel<ShowtimesEvent>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    // Last fetched results, kept so filters apply instantly without refetching.
    private var lastFetch: Fetch? = null
    private var generateJob: Job? = null
    private var refreshJob: Job? = null
    private var detailsJob: Job? = null
    private var personJob: Job? = null
    private var movieIdJob: Job? = null
    private val cardLoader = CardDetailsLoader(viewModelScope, filmDetailsRepository, ::applyCardDetails)

    private class Fetch(
        val showings: List<Showing>,
        val days: Set<LocalDate>,
        val venues: Set<Venue>,
        val filmQueries: List<String>,
        val errors: Map<Venue, VenueErrorUi>
    )

    fun onAction(action: ShowtimesAction) {
        when (action) {
            is ShowtimesAction.OnDayToggle -> { toggleDay(action.date); refresh() }
            is ShowtimesAction.OnDatesConfirm -> { confirmDates(action.dates); refresh() }
            ShowtimesAction.OnCalendarOpen -> _state.update { it.copy(calendar = CalendarUi(minDate = today())) }
            ShowtimesAction.OnCalendarDismiss -> _state.update { it.copy(calendar = null) }
            ShowtimesAction.OnSelectToday -> { selectDays(listOf(today())); refresh() }
            ShowtimesAction.OnSelectTomorrow -> { selectDays(listOf(tomorrow(today()))); refresh() }
            ShowtimesAction.OnSelectWeekend -> { selectDays(upcomingWeekend(today())); refresh() }
            is ShowtimesAction.OnMinTimeChange -> { updateDay(action.date) { it.copy(minTime = action.value) }; refresh() }
            is ShowtimesAction.OnMaxTimeChange -> { updateDay(action.date) { it.copy(maxTime = action.value) }; refresh() }
            is ShowtimesAction.OnFilmFilterChange -> _state.update { it.copy(filmFilter = action.value) }
            is ShowtimesAction.OnVenueToggle -> { toggleVenue(action.venue); refresh() }
            is ShowtimesAction.OnVenuePickerOpen -> openVenuePicker(action.chain)
            ShowtimesAction.OnVenuePickerDismiss -> _state.update { it.copy(venuePicker = null) }
            is ShowtimesAction.OnVenueSearchChange -> _state.update { state ->
                state.copy(venuePicker = state.venuePicker?.copy(query = action.query))
            }
            is ShowtimesAction.OnFormatToggle -> toggleFormat(action.tag)
            ShowtimesAction.OnFormatsReset -> setHiddenTags(emptySet())
            ShowtimesAction.OnGenerateClick -> generate(silent = false)
            ShowtimesAction.OnCopyClick -> copy()
            ShowtimesAction.OnClearClick -> clear()
            ShowtimesAction.OnVenuesReload -> {
                val chain = _state.value.venuePicker?.chain
                if (chain != null) openVenuePicker(chain)
            }
            is ShowtimesAction.OnFilmCardVisible -> cardLoader.load(action.title, resolved = action.title in _state.value.resolvedCards)
            is ShowtimesAction.OnFilmDetailsOpen -> openFilmDetails(action.title)
            ShowtimesAction.OnFilmDetailsDismiss -> dismissFilmDetails()
            is ShowtimesAction.OnPersonOpen -> openPerson(action.person)
            ShowtimesAction.OnPersonDismiss -> dismissPerson()
        }
    }

    // Keeps the time ranges of dates that stay selected.
    private fun selectDays(dates: List<LocalDate>) {
        _state.update { state ->
            state.copy(days = dates.map { date -> state.days.firstOrNull { it.date == date } ?: newDay(date) })
        }
    }

    private fun confirmDates(dates: Set<LocalDate>) {
        selectDays(dates.sorted())
        _state.update { it.copy(calendar = null) }
    }

    private fun toggleDay(date: LocalDate) {
        _state.update { state ->
            val days = if (state.days.any { it.date == date }) {
                state.days.filterNot { it.date == date }
            } else {
                (state.days + newDay(date)).sortedBy { it.date }
            }
            state.copy(days = days)
        }
    }

    private fun toggleVenue(venue: Venue) {
        _state.update { state ->
            val venues = if (venue in state.selectedVenues) state.selectedVenues - venue else state.selectedVenues + venue
            filterPreferences.setSelectedVenues(venues)
            state.copy(selectedVenues = venues)
        }
    }


    private fun openVenuePicker(chain: Chain) {
        _state.update { it.copy(venuePicker = VenuePickerUi(chain = chain)) }
        viewModelScope.launch {
            val catalog = showtimesRepository.getVenues()
            val chainVenues = catalog[chain]
            val catalogUi = when (chainVenues) {
                is Result.Success -> VenueCatalogUi.Loaded(chainVenues.data)
                is Result.Failure -> VenueCatalogUi.Error(chainVenues.error.toUiText())
                null -> VenueCatalogUi.Error(UiText.Resource(Res.string.venues_load_error))
            }
            _state.update { state ->
                state.copy(venuePicker = state.venuePicker?.copy(catalog = catalogUi))
            }
        }
    }

    // Results are already on screen: update them quietly. Cached data is filtered locally;
    // the network is only hit when a day or cinema that was never fetched gets selected.
    // Debounced to avoid excessive fetches during rapid filter changes.
    private fun refresh() {
        val state = _state.value
        if (!state.hasGenerated) return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            val fetch = lastFetch
            val selectedDays = state.days.filter { it.isSelected }.mapTo(mutableSetOf()) { it.date }
            val covered = !state.isLoading && fetch != null && !needsFetch(fetch.days, fetch.venues, selectedDays, state.selectedVenues)
            when {
                covered -> _state.update { it.withResults(fetch) }
                buildRanges(state) != null && selectedDays.isNotEmpty() -> generate(silent = true)
            }
        }
    }

    private fun toggleFormat(tag: String) {
        val hidden = _state.value.hiddenTags
        setHiddenTags(if (tag in hidden) hidden - tag else hidden + tag)
    }

    private fun setHiddenTags(tags: Set<String>) {
        _state.update { it.copy(hiddenTags = tags).withResults(lastFetch) }
    }

    private fun initialState() = freshState(
        selectedVenues = filterPreferences.getSelectedVenues() ?: defaultVenues
    )

    private fun freshState(selectedVenues: Set<Venue>) = ShowtimesState(
        selectedVenues = selectedVenues
    )

    private companion object {
        const val MOVIE_ID_CONCURRENCY = 4
        val defaultVenues = setOf(
            Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto"),
            Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto"),
            Venue(Chain.NOTORIOUS, "fiumeveneto", "Fiume Veneto", "Friuli", "https://www.notoriouscinemas.it/fiumeveneto/index.php")
        )
    }

    private fun newDay(date: LocalDate): DayUi {
        val window = timeWindowController.window.value
        return DayUi(
            date = date,
            isSelected = true,
            minTime = window.minMinutes.toTimeInput(),
            maxTime = window.maxMinutes.toTimeInput()
        )
    }

    private fun updateDay(date: LocalDate, transform: (DayUi) -> DayUi) {
        _state.update { state ->
            state.copy(days = state.days.map { if (it.date == date) transform(it) else it })
        }
    }

    // Ignored while loading: the in-flight generate() would write its results back afterwards.
    private fun clear() {
        if (_state.value.isLoading) return
        lastFetch = null
        movieIdJob?.cancel()
        filterPreferences.setSelectedVenues(emptySet())
        _state.value = freshState(selectedVenues = emptySet())
    }

    private fun copy() {
        val text = _state.value.pollText
        if (text.isBlank()) return
        _events.trySend(ShowtimesEvent.CopyToClipboard(text))
        _events.trySend(ShowtimesEvent.ShowSnackbar(UiText.Resource(Res.string.copied)))
    }

    // A silent run (automatic refresh) skips validation messages; a newer run cancels the one in flight.
    private fun generate(silent: Boolean) {
        val selectedDays = _state.value.days.filter { it.isSelected }
        if (selectedDays.isEmpty()) {
            if (!silent) showSnackbar(UiText.Resource(Res.string.select_day))
            return
        }
        val ranges = buildRanges(_state.value)
        if (ranges == null) {
            if (!silent) showSnackbar(UiText.Resource(Res.string.invalid_time))
            return
        }
        val filmQueries = _state.value.filmFilter.split(",")

        generateJob?.cancel()
        generateJob = viewModelScope.launch {
            val startTimeMillis = clock.now().toEpochMilliseconds()
            _state.update { it.copy(isLoading = true) }

            val venues = _state.value.selectedVenues
            val results = showtimesRepository.getShowings(ranges.map { it.date }, venues)
            val showings = mutableListOf<Showing>()
            val errors = mutableMapOf<Venue, VenueErrorUi>()
            results.forEach { venueShowings ->
                when (val result = venueShowings.result) {
                    is Result.Success -> showings += result.data
                    is Result.Failure -> errors[venueShowings.venue] = VenueErrorUi(
                        venueName = venueShowings.venue.name,
                        message = result.error.toUiText()
                    )
                }
            }

            lastFetch = Fetch(showings, ranges.mapTo(mutableSetOf()) { it.date }, venues, filmQueries, errors)
            val elapsedTimeMillis = clock.now().toEpochMilliseconds() - startTimeMillis
            log.i { "Fetched ${showings.size} showing(s), ${errors.size} venue error(s) in ${elapsedTimeMillis}ms" }
            _state.update {
                it.copy(isLoading = false, hasGenerated = true, elapsedTimeMillis = elapsedTimeMillis).withResults(lastFetch)
            }
            loadMovieIds(showings)
        }
    }

    // Null when a time range is not valid (e.g. still being typed).
    private fun buildRanges(state: ShowtimesState): List<DayRange>? = state.days.filter { it.isSelected }.map { day ->
        val min = parseTimeInput(day.minTime)
        val max = parseTimeInput(day.maxTime)
        if (min is ParsedTime.Invalid || max is ParsedTime.Invalid) return null
        DayRange(day.date, (min as ParsedTime.Valid).value, (max as ParsedTime.Valid).value)
    }

    // Applies the days/times/venues selected now plus the hidden format tags to the last fetched showings.
    // An invalid time range leaves the list as it is.
    private fun ShowtimesState.withResults(fetch: Fetch?): ShowtimesState {
        if (fetch == null) return this
        val ranges = buildRanges(this) ?: return this
        val now = now()
        val showings = fetch.showings.filter { it.venue in selectedVenues }
        val groups = showings
            .filterShowings(ranges, fetch.filmQueries, now, hiddenTags)
            .groupByFilm(movieIds)
        // Tags come from the unfiltered-by-format results, so hidden ones stay toggleable.
        val tags = showings.filterShowings(ranges, fetch.filmQueries, now).availableTags()
        return copy(
            venueErrors = fetch.errors.filterKeys { it in selectedVenues }.values.toList(),
            groups = groups.map { group ->
                FilmGroupUi(
                    title = group.title,
                    posterUrl = group.posterUrl,
                    details = group.details,
                    detailsSources = group.detailsSources,
                    tmdb = cardDetails[group.title],
                    posterPending = group.title !in resolvedCards,
                    showings = group.showings.map { showing ->
                        ShowingUi(
                            day = showing.day.dayOfWeek,
                            date = showing.day.toDateLabel(),
                            time = showing.time.toString(),
                            venue = showing.venue,
                            format = showing.displayFormat,
                            ticketUrl = showing.ticketUrl,
                            soldOut = showing.soldOut
                        )
                    }
                )
            },
            pollText = groups.toPollText(),
            optionsCount = groups.sumOf { group -> group.showings.count { !it.soldOut } },
            availableTags = (tags + hiddenTags).distinct().sorted()
        )
    }

    // Opens in a loading state, without the cinemas' text that the film database's would replace, then shows what the
    // repository resolves: the database's details, or what the cinemas publish when it does not know the film.
    private fun openFilmDetails(title: String) {
        val group = _state.value.groups.firstOrNull { it.title == title } ?: return
        detailsJob?.cancel()
        _state.update { it.copy(filmDetails = FilmDetailsUi(title, FilmDetails(), isLoading = true, posterUrl = group.tmdb?.posterUrl ?: group.posterUrl)) }
        detailsJob = viewModelScope.launch {
            val details = filmDetailsRepository.getFilmDetails(title, group.details, group.detailsSources)
            _state.update { state ->
                if (state.filmDetails?.title == title) state.copy(filmDetails = FilmDetailsUi(title, details, posterUrl = details.posterUrl ?: group.posterUrl)) else state
            }
        }
    }

    // The film database id of each showing's title, so that titles of the same film are grouped together. Resolved once per
    // title (a title that could not be resolved is asked again on the next fetch), then the groups are rebuilt with the ids.
    private fun loadMovieIds(showings: List<Showing>) {
        val titles = showings.map { it.title }.distinct().filterNot { it in _state.value.movieIds }
        movieIdJob?.cancel()
        if (titles.isEmpty()) return
        movieIdJob = viewModelScope.launch {
            val semaphore = Semaphore(MOVIE_ID_CONCURRENCY)
            val ids = coroutineScope {
                titles.map { title -> async { semaphore.withPermit { title to filmDetailsRepository.getMovieId(title) } } }.awaitAll()
            }
            _state.update { state ->
                state.copy(movieIds = state.movieIds + ids.mapNotNull { (title, result) -> (result as? Result.Success)?.data?.let { title to it } }).withResults(lastFetch)
            }
        }
    }

    private fun applyCardDetails(answers: Map<String, FilmDetails?>) {
        _state.update { state ->
            state.copy(
                cardDetails = state.cardDetails + answers.mapNotNull { (title, details) -> details?.let { title to it } },
                resolvedCards = state.resolvedCards + answers.keys,
            ).withResults(lastFetch)
        }
    }

    private fun dismissFilmDetails() {
        detailsJob?.cancel()
        _state.update { it.copy(filmDetails = null) }
    }

    // Shows what the person page has, and the photo the cast row already had, while TMDB answers. A failed lookup leaves
    // the page with the name and the photo only.
    private fun openPerson(person: FilmPerson) {
        personJob?.cancel()
        _state.update { it.copy(personDetails = PersonUi(person, isLoading = true)) }
        personJob = viewModelScope.launch {
            val details = when (val result = personLookup.find(person.id)) {
                is Result.Success -> result.data
                is Result.Failure -> {
                    log.w { "Person lookup failed for ${person.id}: ${result.error}" }
                    null
                }
            }
            _state.update { state ->
                if (state.personDetails?.person == person) state.copy(personDetails = PersonUi(person, details)) else state
            }
        }
    }

    private fun dismissPerson() {
        personJob?.cancel()
        _state.update { it.copy(personDetails = null) }
    }

    private fun showSnackbar(message: UiText) {
        _events.trySend(ShowtimesEvent.ShowSnackbar(message))
    }

    private fun today(): LocalDate = now().date

    private fun now(): LocalDateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun LocalDate.toDateLabel(): String =
        "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"
}

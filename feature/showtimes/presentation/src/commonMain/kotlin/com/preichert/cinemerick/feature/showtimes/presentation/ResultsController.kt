package com.preichert.cinemerick.feature.showtimes.presentation

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.toUiText
import com.preichert.cinemerick.feature.showtimes.domain.DayRange
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
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
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.invalid_time
import com.preichert.cinemerick.feature.showtimes.presentation.resources.select_day
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

private val log = Logger.withTag("ResultsController")
private const val MOVIE_ID_CONCURRENCY = 4

/**
 * The showings of the last generate, and the flows that keep them on screen: generating them, refreshing them when a
 * filter changes, and grouping the titles by their film database id. Only used on the main thread, like the view model.
 */
internal class ResultsController(
    private val scope: CoroutineScope,
    private val showtimesRepository: ShowtimesRepository,
    private val filmDetailsRepository: FilmDetailsRepository,
    private val clock: Clock,
    private val now: () -> LocalDateTime,
    private val read: () -> ShowtimesState,
    private val update: StateUpdate,
    private val message: (UiText) -> Unit,
) {
    // Last fetched results, kept so filters apply instantly without refetching.
    private var lastFetch: Fetch? = null
    private var generateJob: Job? = null
    private var refreshJob: Job? = null
    private var movieIdJob: Job? = null

    private class Fetch(
        val showings: List<Showing>,
        val days: Set<LocalDate>,
        val venues: Set<Venue>,
        val filmQueries: List<String>,
        val errors: Map<Venue, VenueErrorUi>
    )

    // A silent run (automatic refresh) skips validation messages; a newer run cancels the one in flight.
    fun generate(silent: Boolean) {
        val state = read()
        val selectedDays = state.days.filter { it.isSelected }
        if (selectedDays.isEmpty()) {
            if (!silent) message(UiText.Resource(Res.string.select_day))
            return
        }
        val ranges = buildRanges(state)
        if (ranges == null) {
            if (!silent) message(UiText.Resource(Res.string.invalid_time))
            return
        }
        val filmQueries = state.filmFilter.split(",")

        generateJob?.cancel()
        generateJob = scope.launch {
            val startTimeMillis = clock.now().toEpochMilliseconds()
            update { it.copy(isLoading = true) }

            val venues = read().selectedVenues
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
            update { withResults(it.copy(isLoading = false, hasGenerated = true, elapsedTimeMillis = elapsedTimeMillis)) }
            loadMovieIds(showings)
        }
    }

    // Results are already on screen: update them quietly. Cached data is filtered locally;
    // the network is only hit when a day or cinema that was never fetched gets selected.
    // Debounced to avoid excessive fetches during rapid filter changes.
    fun refresh() {
        val state = read()
        if (!state.hasGenerated) return
        refreshJob?.cancel()
        refreshJob = scope.launch {
            delay(500)
            val fetch = lastFetch
            val selectedDays = state.days.filter { it.isSelected }.mapTo(mutableSetOf()) { it.date }
            val covered = !state.isLoading && fetch != null && !needsFetch(fetch.days, fetch.venues, selectedDays, state.selectedVenues)
            when {
                covered -> update { resultsOf(it, fetch) }
                buildRanges(state) != null && selectedDays.isNotEmpty() -> generate(silent = true)
            }
        }
    }

    // Forgets the fetched showings, so the next generate starts from nothing.
    fun clear() {
        lastFetch = null
        movieIdJob?.cancel()
    }

    // Applies the days, times, venues and hidden format tags now selected to the last fetched showings.
    fun withResults(state: ShowtimesState): ShowtimesState = resultsOf(state, lastFetch)

    // The film database id of each showing's title, so that titles of the same film are grouped together. Resolved once per
    // title (a title that could not be resolved is asked again on the next fetch), then the groups are rebuilt with the ids.
    private fun loadMovieIds(showings: List<Showing>) {
        val titles = showings.map { it.title }.distinct().filterNot { it in read().movieIds }
        movieIdJob?.cancel()
        if (titles.isEmpty()) return
        movieIdJob = scope.launch {
            val semaphore = Semaphore(MOVIE_ID_CONCURRENCY)
            val ids = coroutineScope {
                titles.map { title -> async { semaphore.withPermit { title to filmDetailsRepository.getMovieId(title) } } }.awaitAll()
            }
            update { state ->
                withResults(state.copy(movieIds = state.movieIds + ids.mapNotNull { (title, result) -> (result as? Result.Success)?.data?.let { title to it } }))
            }
        }
    }

    // Null when a time range is not valid (e.g. still being typed).
    private fun buildRanges(state: ShowtimesState): List<DayRange>? = state.days.filter { it.isSelected }.map { day ->
        val min = parseTimeInput(day.minTime)
        val max = parseTimeInput(day.maxTime)
        if (min is ParsedTime.Invalid || max is ParsedTime.Invalid) return null
        DayRange(day.date, (min as ParsedTime.Valid).value, (max as ParsedTime.Valid).value)
    }

    // An invalid time range leaves the list as it is.
    private fun resultsOf(state: ShowtimesState, fetch: Fetch?): ShowtimesState {
        if (fetch == null) return state
        val ranges = buildRanges(state) ?: return state
        val currentTime = now()
        return with(state) {
            val showings = fetch.showings.filter { it.venue in selectedVenues }
            val groups = showings
                .filterShowings(ranges, fetch.filmQueries, currentTime, hiddenTags)
                .groupByFilm(movieIds)
            // Tags come from the unfiltered-by-format results, so hidden ones stay toggleable.
            val tags = showings.filterShowings(ranges, fetch.filmQueries, currentTime).availableTags()
            copy(
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
    }

    private fun LocalDate.toDateLabel(): String =
        "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"
}

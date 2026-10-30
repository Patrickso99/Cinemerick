package com.preichert.cinemerick.feature.showtimes.presentation

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val log = Logger.withTag("DetailsLoaders")

// Applies a change to the screen state, as the view model's state does.
internal typealias StateUpdate = ((ShowtimesState) -> ShowtimesState) -> Unit

/**
 * The film details overlay. It opens in a loading state, without the cinemas' text that the film database's would replace,
 * then shows what the repository resolves: the database's details, or what the cinemas publish when it does not know the film.
 */
internal class FilmDetailsLoader(
    private val scope: CoroutineScope,
    private val filmDetailsRepository: FilmDetailsRepository,
    private val update: StateUpdate,
) {
    private var job: Job? = null

    fun open(group: FilmGroupUi) {
        val title = group.title
        job?.cancel()
        update { it.copy(filmDetails = FilmDetailsUi(title, FilmDetails(), isLoading = true, posterUrl = group.tmdb?.posterUrl ?: group.posterUrl)) }
        job = scope.launch {
            val details = filmDetailsRepository.getFilmDetails(title, group.details, group.detailsSources)
            update { state ->
                if (state.filmDetails?.title == title) state.copy(filmDetails = FilmDetailsUi(title, details, posterUrl = details.posterUrl ?: group.posterUrl)) else state
            }
        }
    }

    fun dismiss() {
        job?.cancel()
        update { it.copy(filmDetails = null) }
    }
}

/**
 * The person details overlay. It shows what the person page has, and the photo the cast row already had, while TMDB answers.
 * A failed lookup leaves the page with the name and the photo only.
 */
internal class PersonDetailsLoader(
    private val scope: CoroutineScope,
    private val personLookup: PersonLookup,
    private val update: StateUpdate,
) {
    private var job: Job? = null

    fun open(person: FilmPerson) {
        job?.cancel()
        update { it.copy(personDetails = PersonUi(person, isLoading = true)) }
        job = scope.launch {
            val details = when (val result = personLookup.find(person.id)) {
                is Result.Success -> result.data
                is Result.Failure -> {
                    log.w { "Person lookup failed for ${person.id}: ${result.error}" }
                    null
                }
            }
            update { state ->
                if (state.personDetails?.person == person) state.copy(personDetails = PersonUi(person, details)) else state
            }
        }
    }

    fun dismiss() {
        job?.cancel()
        update { it.copy(personDetails = null) }
    }
}

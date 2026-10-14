package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmLookup
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.filmKey
import com.preichert.cinemerick.feature.showtimes.domain.searchTitle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val log = Logger.withTag("FilmDetailsRepository")

/**
 * What the film database says about a film, and only when it knows the film. Otherwise (no match, or the database could
 * not be asked) what the cinemas publish, through [showtimesRepository]. The two are never mixed.
 */
class TmdbFirstFilmDetailsRepository(
    private val lookup: FilmLookup,
    private val showtimesRepository: ShowtimesRepository
) : FilmDetailsRepository {

    // Outcomes of the lookup for the session, by film key: a "no match" is remembered (null), a failure is not.
    private class Outcome(val details: FilmDetails?)
    private val outcomes = mutableMapOf<String, Outcome>()
    private val outcomesLock = Mutex()

    override suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>): FilmDetails =
        databaseDetails(title) ?: showtimesRepository.getFilmDetails(known, sources)

    private suspend fun databaseDetails(title: String): FilmDetails? {
        val key = filmKey(title)
        outcomesLock.withLock { outcomes[key] }?.let { return it.details }
        return when (val result = lookup.find(searchTitle(title))) {
            // A match that carries nothing is as good as no match.
            is Result.Success -> result.data?.takeUnless { it.isEmpty }
                .also { details -> outcomesLock.withLock { outcomes[key] = Outcome(details) } }
            is Result.Failure -> {
                log.w { "Film lookup failed for \"$title\": ${result.error}" }
                null
            }
        }
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.DetailsSource
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("ShowtimesRepository")

class MultiCinemaShowtimesRepository(
    private val dataSources: List<ShowtimesDataSource>,
    private val cache: ShowtimesCache = ShowtimesCache()
) : ShowtimesRepository {

    // Film pages rarely change and are only fetched on demand: kept for the whole session, by page URL.
    private val detailsByUrl = mutableMapOf<String, FilmDetails>()
    private val detailsLock = Mutex()

    override suspend fun getVenues(): Map<Chain, Result<List<Venue>, DataError.Network>> = coroutineScope {
        log.d { "Fetching venues for ${dataSources.size} chain(s)" }
        dataSources
            .map { dataSource -> async { dataSource.chain to dataSource.getVenues() } }
            .awaitAll()
            .toMap()
    }

    override suspend fun getShowings(days: List<LocalDate>, venues: Set<Venue>): List<VenueShowings> = coroutineScope {
        log.d { "Fetching ${venues.joinToString { it.name }} for ${days.size} day(s)" }
        venues
            .groupBy { it.chain }
            .flatMap { (chain, venuesByChain) ->
                val dataSource = dataSources.firstOrNull { it.chain == chain } ?: return@flatMap emptyList()
                venuesByChain.map { venue ->
                    async {
                        val cachedShowings = days.mapNotNull { day ->
                            cache.get(venue, day)?.let { day to it }
                        }.toMap()

                        val uncachedDays = days - cachedShowings.keys
                        val fetchedResult = if (uncachedDays.isNotEmpty()) {
                            dataSource.getShowings(venue, uncachedDays)
                        } else {
                            Result.Success(emptyList())
                        }

                        when (fetchedResult) {
                            is Result.Success -> {
                                fetchedResult.data.groupBy { it.day }.forEach { (day, showings) ->
                                    cache.put(venue, day, showings)
                                }
                            }
                            is Result.Failure -> log.w { "Failed to fetch showings for ${venue.name}: ${fetchedResult.error}" }
                        }

                        // A failed fetch only surfaces as a venue error when nothing cached can stand in for it.
                        val failure = (fetchedResult as? Result.Failure)?.error
                        if (failure != null && cachedShowings.isEmpty()) {
                            return@async VenueShowings(venue, Result.Failure(failure))
                        }

                        val allShowings = (cachedShowings.values.flatten() +
                                (fetchedResult as? Result.Success)?.data.orEmpty()).distinctBy { "${it.day}:${it.time}:${it.title}" }
                        VenueShowings(venue, Result.Success(allShowings))
                    }
                }
            }
            .awaitAll()
    }

    override suspend fun getFilmDetails(known: FilmDetails, sources: List<DetailsSource>): FilmDetails {
        if (known.isComplete || sources.isEmpty()) return known
        val fetched = coroutineScope {
            sources.map { source -> async { fetchFilmDetails(source) } }.awaitAll()
        }
        // `sources` come best chain first, so earlier pages win field by field.
        return fetched.fold(known) { merged, details -> merged.withFallback(details) }
    }

    private suspend fun fetchFilmDetails(source: DetailsSource): FilmDetails {
        detailsLock.withLock { detailsByUrl[source.url] }?.let { return it }
        val dataSource = dataSources.firstOrNull { it.chain == source.chain } ?: return FilmDetails()
        return when (val result = dataSource.getFilmDetails(source.url)) {
            is Result.Success -> result.data.also { details -> detailsLock.withLock { detailsByUrl[source.url] = details } }
            is Result.Failure -> {
                log.w { "Failed to fetch film page ${source.url}: ${result.error}" }
                FilmDetails()
            }
        }
    }
}

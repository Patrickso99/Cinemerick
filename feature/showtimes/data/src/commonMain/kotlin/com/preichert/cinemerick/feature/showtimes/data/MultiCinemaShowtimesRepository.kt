package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueShowings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("ShowtimesRepository")

class MultiCinemaShowtimesRepository(
    private val dataSources: List<ShowtimesDataSource>,
    private val cache: ShowtimesCache = ShowtimesCache()
) : ShowtimesRepository {

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

                        val allShowings = (cachedShowings.values.flatten() +
                                (fetchedResult as? Result.Success)?.data.orEmpty()).distinctBy { "${it.day}:${it.time}:${it.title}" }
                        VenueShowings(venue, Result.Success(allShowings))
                    }
                }
            }
            .awaitAll()
    }
}

package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.CinemaShowings
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

private val log = Logger.withTag("ShowtimesRepository")

class MultiCinemaShowtimesRepository(
    private val dataSources: List<ShowtimesDataSource>
) : ShowtimesRepository {

    override suspend fun getShowings(days: List<LocalDate>, cinemas: Set<Cinema>): List<CinemaShowings> = coroutineScope {
        log.d { "Fetching ${cinemas.joinToString { it.displayName }} for ${days.size} day(s)" }
        dataSources
            .filter { it.cinema in cinemas }
            .map { dataSource -> async { CinemaShowings(dataSource.cinema, dataSource.getShowings(days)) } }
            .awaitAll()
    }
}

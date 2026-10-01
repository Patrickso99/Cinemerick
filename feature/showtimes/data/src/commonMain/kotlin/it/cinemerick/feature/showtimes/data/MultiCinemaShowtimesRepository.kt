package it.cinemerick.feature.showtimes.data

import it.cinemerick.feature.showtimes.domain.CinemaShowings
import it.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import it.cinemerick.feature.showtimes.domain.ShowtimesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

class MultiCinemaShowtimesRepository(
    private val dataSources: List<ShowtimesDataSource>
) : ShowtimesRepository {

    override suspend fun getShowings(days: List<LocalDate>): List<CinemaShowings> = coroutineScope {
        dataSources
            .map { dataSource -> async { CinemaShowings(dataSource.cinema, dataSource.getShowings(days)) } }
            .awaitAll()
    }
}

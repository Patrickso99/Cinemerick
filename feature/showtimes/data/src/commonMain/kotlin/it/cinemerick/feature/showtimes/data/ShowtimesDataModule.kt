package it.cinemerick.feature.showtimes.data

import it.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import it.cinemerick.feature.showtimes.domain.ShowtimesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val showtimesDataModule = module {
    singleOf(::SpaceTokenProvider)
    singleOf(::KtorUciShowtimesDataSource) bind ShowtimesDataSource::class
    singleOf(::KtorSpaceShowtimesDataSource) bind ShowtimesDataSource::class
    single<ShowtimesRepository> { MultiCinemaShowtimesRepository(getAll()) }
}

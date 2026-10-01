package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val showtimesDataModule = module {
    singleOf(::TheSpaceTokenProvider)
    singleOf(::KtorUciShowtimesDataSource) bind ShowtimesDataSource::class
    singleOf(::KtorNotoriousShowtimesDataSource) bind ShowtimesDataSource::class
    singleOf(::KtorCinergiaShowtimesDataSource) bind ShowtimesDataSource::class
    singleOf(::KtorTheSpaceShowtimesDataSource) bind ShowtimesDataSource::class
    single<ShowtimesRepository> { MultiCinemaShowtimesRepository(getAll()) }
}

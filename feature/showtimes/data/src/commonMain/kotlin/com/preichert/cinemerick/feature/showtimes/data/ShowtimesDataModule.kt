package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val showtimesDataModule = module {
    singleOf(::SettingsFilterPreferences) { bind<FilterPreferences>() }
    singleOf(::TheSpaceTokenProvider)
    single { ShowtimesCache(clock = get()) }

    singleOf(::KtorUciShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorNotoriousShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorCinergiaShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorCristalloShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorDonBoscoShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorCinemazeroShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorArcadiaShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }
    singleOf(::KtorTheSpaceShowtimesDataSource) { bind<ShowtimesRemoteDataSource>() }

    singleOf(::SettingsTmdbMatchDataSource) { bind<TmdbMatchLocalDataSource>() }
    single<FilmRemoteDataSource> { KtorTmdbFilmDataSource(get(), get()) }
    single<PersonRemoteDataSource> { KtorTmdbPersonDataSource(get()) }

    single<ShowtimesRepository> { MultiCinemaShowtimesRepository(getAll(), get()) }
    singleOf(::TmdbFirstFilmDetailsRepository) { bind<FilmDetailsRepository>() }
}

package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilmLookup
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val showtimesDataModule = module {
    singleOf(::SettingsFilterPreferences) { bind<FilterPreferences>() }
    singleOf(::TheSpaceTokenProvider)

    singleOf(::KtorUciShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorNotoriousShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorCinergiaShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorCristalloShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorDonBoscoShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorCinemazeroShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorArcadiaShowtimesDataSource) { bind<ShowtimesDataSource>() }
    singleOf(::KtorTheSpaceShowtimesDataSource) { bind<ShowtimesDataSource>() }

    single<TmdbMatchStore> { SettingsTmdbMatchStore(get()) }
    single<FilmLookup> { KtorTmdbFilmLookup(get(), get()) }
    single<PersonLookup> { KtorTmdbPersonLookup(get()) }

    singleOf(::MultiCinemaShowtimesRepository) { bind<ShowtimesRepository>() }
    singleOf(::TmdbFirstFilmDetailsRepository) { bind<FilmDetailsRepository>() }
}

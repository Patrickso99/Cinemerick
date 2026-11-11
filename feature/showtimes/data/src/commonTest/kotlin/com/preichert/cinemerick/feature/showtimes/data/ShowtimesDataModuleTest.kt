package com.preichert.cinemerick.feature.showtimes.data

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isInstanceOf
import com.preichert.cinemerick.core.data.DefaultDispatcherProvider
import com.preichert.cinemerick.core.data.DispatcherProvider
import com.preichert.cinemerick.core.testing.FakeClock
import com.preichert.cinemerick.core.testing.MemorySettings
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.PersonRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRemoteDataSource
import com.preichert.cinemerick.feature.showtimes.domain.ShowtimesRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.time.Clock

class ShowtimesDataModuleTest {

    // What core:data provides in the app.
    private val coreStub = module {
        single { HttpClient(MockEngine { respondOk() }) }
        single<DispatcherProvider> { DefaultDispatcherProvider() }
        single<Settings> { MemorySettings() }
        single<Clock> { FakeClock() }
    }

    private val koin = koinApplication { modules(coreStub, showtimesDataModule) }.koin

    @Test
    fun everyChainHasOneDataSource() {
        val chains = koin.getAll<ShowtimesRemoteDataSource>().map { it.chain }

        assertThat(chains).containsExactlyInAnyOrder(*Chain.entries.toTypedArray())
    }

    @Test
    fun whatTheViewModelAsksForIsResolved() {
        assertThat(koin.get<ShowtimesRepository>()).isInstanceOf<MultiCinemaShowtimesRepository>()
        assertThat(koin.get<FilmDetailsRepository>()).isInstanceOf<TmdbFirstFilmDetailsRepository>()
        assertThat(koin.get<PersonRemoteDataSource>()).isInstanceOf<KtorTmdbPersonDataSource>()
        assertThat(koin.get<FilterPreferences>()).isInstanceOf<SettingsFilterPreferences>()
    }
}

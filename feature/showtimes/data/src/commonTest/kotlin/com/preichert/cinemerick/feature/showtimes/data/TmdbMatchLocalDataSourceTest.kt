package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.testing.FakeClock
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

class TmdbMatchLocalDataSourceTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun aRememberedMatchIsReadBackByAnotherStoreOnTheSameSettings() {
        val settings = MemorySettings()
        SettingsTmdbMatchDataSource(settings, FakeClock(0L)).put("starwars", RememberedMatch.Found(11))

        assertThat(SettingsTmdbMatchDataSource(settings, FakeClock(1L)).get("starwars")).isEqualTo(RememberedMatch.Found(11))
    }

    @Test
    fun aMatchNeverExpires() {
        val clock = FakeClock()
        val store = SettingsTmdbMatchDataSource(MemorySettings(), clock)
        store.put("starwars", RememberedMatch.Found(11))

        clock.millis = 1000L * day

        assertThat(store.get("starwars")).isEqualTo(RememberedMatch.Found(11))
    }

    @Test
    fun aNoMatchExpiresAfterAWeek() {
        val clock = FakeClock()
        val store = SettingsTmdbMatchDataSource(MemorySettings(), clock)
        store.put("mammina", RememberedMatch.NoMatch)

        clock.millis = 7 * day
        assertThat(store.get("mammina")).isEqualTo(RememberedMatch.NoMatch)

        clock.millis = 7 * day + 1
        assertThat(store.get("mammina")).isNull()
    }

    @Test
    fun anUnknownTitleIsNotRemembered() {
        assertThat(SettingsTmdbMatchDataSource(MemorySettings(), FakeClock(0L)).get("mammina")).isNull()
    }

    @Test
    fun aDamagedValueIsIgnored() {
        val settings = MemorySettings()
        settings.putString("tmdb_match.starwars", "garbage")
        settings.putString("tmdb_match.mammina", "none|not-a-time")

        val store = SettingsTmdbMatchDataSource(settings, FakeClock(0L))

        assertThat(store.get("starwars")).isNull()
        assertThat(store.get("mammina")).isNull()
    }
}

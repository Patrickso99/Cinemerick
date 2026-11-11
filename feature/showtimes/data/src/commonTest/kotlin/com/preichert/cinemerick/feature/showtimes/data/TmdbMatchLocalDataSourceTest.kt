package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.testing.FakeClock
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbMatchLocalDataSourceTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun aRememberedMatchIsReadBackByAnotherStoreOnTheSameSettings() {
        val settings = MemorySettings()
        SettingsTmdbMatchDataSource(settings, FakeClock(0L)).put("starwars", RememberedMatch.Found(11))

        assertEquals(RememberedMatch.Found(11), SettingsTmdbMatchDataSource(settings, FakeClock(1L)).get("starwars"))
    }

    @Test
    fun aMatchNeverExpires() {
        val clock = FakeClock()
        val store = SettingsTmdbMatchDataSource(MemorySettings(), clock)
        store.put("starwars", RememberedMatch.Found(11))

        clock.millis = 1000L * day

        assertEquals(RememberedMatch.Found(11), store.get("starwars"))
    }

    @Test
    fun aNoMatchExpiresAfterAWeek() {
        val clock = FakeClock()
        val store = SettingsTmdbMatchDataSource(MemorySettings(), clock)
        store.put("mammina", RememberedMatch.NoMatch)

        clock.millis = 7 * day
        assertEquals(RememberedMatch.NoMatch, store.get("mammina"))

        clock.millis = 7 * day + 1
        assertNull(store.get("mammina"))
    }

    @Test
    fun anUnknownTitleIsNotRemembered() {
        assertNull(SettingsTmdbMatchDataSource(MemorySettings(), FakeClock(0L)).get("mammina"))
    }

    @Test
    fun aDamagedValueIsIgnored() {
        val settings = MemorySettings()
        settings.putString("tmdb_match.starwars", "garbage")
        settings.putString("tmdb_match.mammina", "none|not-a-time")

        val store = SettingsTmdbMatchDataSource(settings, FakeClock(0L))

        assertNull(store.get("starwars"))
        assertNull(store.get("mammina"))
    }
}

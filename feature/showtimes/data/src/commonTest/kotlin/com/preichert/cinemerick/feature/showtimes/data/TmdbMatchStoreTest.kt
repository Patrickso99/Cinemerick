package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbMatchStoreTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun aRememberedMatchIsReadBackByAnotherStoreOnTheSameSettings() {
        val settings = MemorySettings()
        SettingsTmdbMatchStore(settings) { 0L }.put("starwars", RememberedMatch.Found(11))

        assertEquals(RememberedMatch.Found(11), SettingsTmdbMatchStore(settings) { 1L }.get("starwars"))
    }

    @Test
    fun aMatchNeverExpires() {
        var now = 0L
        val store = SettingsTmdbMatchStore(MemorySettings()) { now }
        store.put("starwars", RememberedMatch.Found(11))

        now = 1000L * day

        assertEquals(RememberedMatch.Found(11), store.get("starwars"))
    }

    @Test
    fun aNoMatchExpiresAfterAWeek() {
        var now = 0L
        val store = SettingsTmdbMatchStore(MemorySettings()) { now }
        store.put("mammina", RememberedMatch.NoMatch)

        now = 7 * day
        assertEquals(RememberedMatch.NoMatch, store.get("mammina"))

        now = 7 * day + 1
        assertNull(store.get("mammina"))
    }

    @Test
    fun anUnknownTitleIsNotRemembered() {
        assertNull(SettingsTmdbMatchStore(MemorySettings()) { 0L }.get("mammina"))
    }

    @Test
    fun aDamagedValueIsIgnored() {
        val settings = MemorySettings()
        settings.putString("tmdb_match.starwars", "garbage")
        settings.putString("tmdb_match.mammina", "none|not-a-time")

        val store = SettingsTmdbMatchStore(settings) { 0L }

        assertNull(store.get("starwars"))
        assertNull(store.get("mammina"))
    }
}

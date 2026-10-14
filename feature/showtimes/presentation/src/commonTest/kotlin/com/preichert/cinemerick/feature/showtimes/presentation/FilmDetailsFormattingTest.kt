package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilmDetailsFormattingTest {

    @Test
    fun runtimeIsShownInHoursAndMinutes() {
        assertEquals("45 min", formatRuntime(45))
        assertEquals("1 h 00 min", formatRuntime(60))
        assertEquals("2 h 05 min", formatRuntime(125))
        assertEquals("2 h 01 min", formatRuntime(121))
    }

    @Test
    fun metaLineJoinsYearAndRuntimeAndSkipsWhatIsMissing() {
        assertEquals("1977 · 2 h 01 min", FilmDetails(year = 1977, runtimeMinutes = 121).metaLine())
        assertEquals("1977", FilmDetails(year = 1977).metaLine())
        assertEquals("1 h 35 min", FilmDetails(runtimeMinutes = 95).metaLine())
        assertNull(FilmDetails(synopsis = "plot").metaLine())
    }

    @Test
    fun scoreHasOneDecimalAndADot() {
        assertEquals("8.2", formatScore(8.207))
        assertEquals("7.0", formatScore(7.0))
        assertEquals("6.5", formatScore(6.5))
        assertEquals("10.0", formatScore(9.97))
        assertEquals("0.0", formatScore(0.0))
    }

    @Test
    fun originalTitleIsHiddenWhenItRepeatsTheShownTitle() {
        assertEquals("Star Wars", FilmDetails(originalTitle = "Star Wars").originalTitleFor("Guerre stellari"))
        assertNull(FilmDetails(originalTitle = "Mammina").originalTitleFor("Mammina"))
        assertNull(FilmDetails(originalTitle = "MAMMINA").originalTitleFor(" mammina "))
        assertNull(FilmDetails().originalTitleFor("Mammina"))
    }
}

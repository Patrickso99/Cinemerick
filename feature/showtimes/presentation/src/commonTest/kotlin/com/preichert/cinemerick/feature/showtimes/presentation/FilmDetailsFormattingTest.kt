package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
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

    @Test
    fun aKnownLanguageCodeHasAPlatformNameThatIsNotTheCode() {
        val name = languageDisplayName("en")
        assertNotNull(name)
        assertNotEquals("EN", name.uppercase())
        assertEquals(name, languageDisplayName(" EN "))
    }

    @Test
    fun unknownLanguageCodeHasNoNameAndFallsBackToTheUpperCaseCode() {
        assertNull(languageDisplayName("xx"))
        assertNull(languageDisplayName(""))
        assertEquals("XX", languageCodeFallback("xx"))
    }

    @Test
    fun moneyIsShortInDollars() {
        assertEquals("$190M", formatMoney(190_000_000))
        assertEquals("$715M", formatMoney(714_844_358))
        assertEquals("$11M", formatMoney(11_000_000))
        assertEquals("$5.5M", formatMoney(5_500_000))
        assertEquals("$1.2B", formatMoney(1_234_000_000))
        assertEquals("$2B", formatMoney(2_000_000_000))
        assertEquals("$850K", formatMoney(850_000))
        assertEquals("$500", formatMoney(500))
    }

    @Test
    fun moneyNeverRoundsUpToAThousandOfTheSmallerUnit() {
        assertEquals("$1B", formatMoney(999_600_000))
        assertEquals("$10M", formatMoney(9_960_000))
        assertEquals("$1M", formatMoney(999_000))
    }

    @Test
    fun releaseDateHasDayMonthYearWithLeadingZeros() {
        assertEquals("28/02/2024", formatReleaseDate(LocalDate(2024, 2, 28)))
        assertEquals("01/03/2024", formatReleaseDate(LocalDate(2024, 3, 1)))
    }
}

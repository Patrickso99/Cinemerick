package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull

class FilmDetailsFormattingTest {

    @Test
    fun runtimeIsShownInHoursAndMinutes() {
        assertThat(formatRuntime(45)).isEqualTo("45 min")
        assertThat(formatRuntime(60)).isEqualTo("1 h 00 min")
        assertThat(formatRuntime(125)).isEqualTo("2 h 05 min")
        assertThat(formatRuntime(121)).isEqualTo("2 h 01 min")
    }

    @Test
    fun metaLineJoinsYearAndRuntimeAndSkipsWhatIsMissing() {
        assertThat(FilmDetails(year = 1977, runtimeMinutes = 121).metaLine()).isEqualTo("1977 · 2 h 01 min")
        assertThat(FilmDetails(year = 1977).metaLine()).isEqualTo("1977")
        assertThat(FilmDetails(runtimeMinutes = 95).metaLine()).isEqualTo("1 h 35 min")
        assertThat(FilmDetails(synopsis = "plot").metaLine()).isNull()
    }

    @Test
    fun scoreHasOneDecimalAndADot() {
        assertThat(formatScore(8.207)).isEqualTo("8.2")
        assertThat(formatScore(7.0)).isEqualTo("7.0")
        assertThat(formatScore(6.5)).isEqualTo("6.5")
        assertThat(formatScore(9.97)).isEqualTo("10.0")
        assertThat(formatScore(0.0)).isEqualTo("0.0")
    }

    @Test
    fun originalTitleIsHiddenWhenItRepeatsTheShownTitle() {
        assertThat(FilmDetails(originalTitle = "Star Wars").originalTitleFor("Guerre stellari")).isEqualTo("Star Wars")
        assertThat(FilmDetails(originalTitle = "Mammina").originalTitleFor("Mammina")).isNull()
        assertThat(FilmDetails(originalTitle = "MAMMINA").originalTitleFor(" mammina ")).isNull()
        assertThat(FilmDetails().originalTitleFor("Mammina")).isNull()
    }

    @Test
    fun aKnownLanguageCodeHasAPlatformNameThatIsNotTheCode() {
        val name = languageDisplayName("en")
        assertThat(name).isNotNull()
        assertThat(name!!.uppercase()).isNotEqualTo("EN")
        assertThat(languageDisplayName(" EN ")).isEqualTo(name)
    }

    @Test
    fun unknownLanguageCodeHasNoNameAndFallsBackToTheUpperCaseCode() {
        assertThat(languageDisplayName("xx")).isNull()
        assertThat(languageDisplayName("")).isNull()
        assertThat(languageCodeFallback("xx")).isEqualTo("XX")
    }

    @Test
    fun moneyIsShortInDollars() {
        assertThat(formatMoney(190_000_000)).isEqualTo("$190M")
        assertThat(formatMoney(714_844_358)).isEqualTo("$715M")
        assertThat(formatMoney(11_000_000)).isEqualTo("$11M")
        assertThat(formatMoney(5_500_000)).isEqualTo("$5.5M")
        assertThat(formatMoney(1_234_000_000)).isEqualTo("$1.2B")
        assertThat(formatMoney(2_000_000_000)).isEqualTo("$2B")
        assertThat(formatMoney(850_000)).isEqualTo("$850K")
        assertThat(formatMoney(500)).isEqualTo("$500")
    }

    @Test
    fun moneyNeverRoundsUpToAThousandOfTheSmallerUnit() {
        assertThat(formatMoney(999_600_000)).isEqualTo("$1B")
        assertThat(formatMoney(9_960_000)).isEqualTo("$10M")
        assertThat(formatMoney(999_000)).isEqualTo("$1M")
    }

    @Test
    fun releaseDateHasDayMonthYearWithLeadingZeros() {
        assertThat(formatReleaseDate(LocalDate(2024, 2, 28))).isEqualTo("28/02/2024")
        assertThat(formatReleaseDate(LocalDate(2024, 3, 1))).isEqualTo("01/03/2024")
    }
}

package com.preichert.cinemerick.feature.showtimes.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalTime
import kotlin.test.Test

/*
 * Break vectors for parseTimeInput, one angle per test:
 * blank input, hour-only input, the 24:00 end-of-day boundary, out-of-range values,
 * non-numeric shapes, digit-count guards, extra separators, interior whitespace, Unicode digits.
 */
class TimeParsingTest {

    @Test
    fun blankOrWhitespaceOnlyMeansNoBound() {
        assertThat(parseTimeInput("")).isEqualTo(ParsedTime.Valid(null))
        assertThat(parseTimeInput("   ")).isEqualTo(ParsedTime.Valid(null))
        assertThat(parseTimeInput("\t\n")).isEqualTo(ParsedTime.Valid(null))
    }

    @Test
    fun hourOnlyMeansOnTheHour() {
        assertThat(parseTimeInput("9")).isEqualTo(ParsedTime.Valid(LocalTime(9, 0)))
        assertThat(parseTimeInput("09")).isEqualTo(ParsedTime.Valid(LocalTime(9, 0)))
    }

    @Test
    fun outerWhitespaceIsTrimmed() {
        assertThat(parseTimeInput("  21:30  ")).isEqualTo(ParsedTime.Valid(LocalTime(21, 30)))
    }

    @Test
    fun twentyFourAloneIsEndOfDay() {
        assertThat(parseTimeInput("24")).isEqualTo(ParsedTime.Valid(LocalTime(23, 59)))
        assertThat(parseTimeInput("24:00")).isEqualTo(ParsedTime.Valid(LocalTime(23, 59)))
    }

    @Test
    fun twentyFourWithMinutesIsInvalid() {
        assertThat(parseTimeInput("24:01")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun hourAndMinuteOutOfRangeAreInvalid() {
        assertThat(parseTimeInput("25")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("23:60")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun signedNumbersAreInvalid() {
        // toIntOrNull accepts a leading sign, so the digit check is what rejects these.
        assertThat(parseTimeInput("-1")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("+1")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("1:-5")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun nonNumericShapesAreInvalid() {
        assertThat(parseTimeInput("1,5")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("1.5")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("1e5")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("0x10")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("NaN")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun hourLongerThanTwoDigitsIsInvalid() {
        assertThat(parseTimeInput("007")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("123")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun minutesMustBeExactlyTwoDigits() {
        assertThat(parseTimeInput("9:5")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("9:005")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("12:")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun missingHourIsInvalid() {
        assertThat(parseTimeInput(":30")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun extraSeparatorsAreInvalid() {
        assertThat(parseTimeInput("1:2:3")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("12::30")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun interiorWhitespaceIsNotTrimmed() {
        assertThat(parseTimeInput("12 :30")).isEqualTo(ParsedTime.Invalid)
        assertThat(parseTimeInput("1 2")).isEqualTo(ParsedTime.Invalid)
    }

    @Test
    fun arabicIndicDigitsAreNotAcceptedAsTime() {
        // Character.isDigit is true for non-ASCII digits and toIntOrNull parses them too,
        // so the parser only accepts ASCII digits.
        assertThat(parseTimeInput("١٢")).isEqualTo(ParsedTime.Invalid)
    }
}

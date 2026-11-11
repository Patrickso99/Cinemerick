package com.preichert.cinemerick.core.domain

import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue

class TimeWindowPreferencesTest {

    @Test
    fun minBelowMaxIsValid() = assertThat(isValidWindow(1200, 1380)).isTrue()

    @Test
    fun equalBoundsAreValid() = assertThat(isValidWindow(1200, 1200)).isTrue()

    @Test
    fun minAboveMaxIsInvalid() = assertThat(isValidWindow(1380, 1320)).isFalse()

    @Test
    fun missingMaxIsAlwaysValid() = assertThat(isValidWindow(23 * 60 + 59, null)).isTrue()
}

package com.preichert.cinemerick.core.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeWindowPreferencesTest {

    @Test
    fun minBelowMaxIsValid() = assertTrue(isValidWindow(1200, 1380))

    @Test
    fun equalBoundsAreValid() = assertTrue(isValidWindow(1200, 1200))

    @Test
    fun minAboveMaxIsInvalid() = assertFalse(isValidWindow(1380, 1320))

    @Test
    fun missingMaxIsAlwaysValid() = assertTrue(isValidWindow(23 * 60 + 59, null))
}

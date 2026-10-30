package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.presentation.resources.Res
import com.preichert.cinemerick.core.presentation.resources.error_unknown
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UiTextTest {

    @Test
    fun aDynamicStringIsItsOwnText() = runTest {
        assertEquals("Connection lost", UiText.DynamicString("Connection lost").asStringAsync())
    }

    @Test
    fun resourcesCompareByIdAndArguments() {
        assertEquals(UiText.Resource(Res.string.error_unknown, listOf("x")), UiText.Resource(Res.string.error_unknown, listOf("x")))
        assertNotEquals(UiText.Resource(Res.string.error_unknown, listOf("x")), UiText.Resource(Res.string.error_unknown, listOf("y")))
    }
}

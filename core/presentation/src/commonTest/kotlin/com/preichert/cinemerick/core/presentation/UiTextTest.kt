package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.presentation.resources.Res
import com.preichert.cinemerick.core.presentation.resources.error_unknown
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo

class UiTextTest {

    @Test
    fun aDynamicStringIsItsOwnText() = runTest {
        assertThat(UiText.DynamicString("Connection lost").asStringAsync()).isEqualTo("Connection lost")
    }

    @Test
    fun resourcesCompareByIdAndArguments() {
        assertThat(UiText.Resource(Res.string.error_unknown, listOf("x"))).isEqualTo(UiText.Resource(Res.string.error_unknown, listOf("x")))
        assertThat(UiText.Resource(Res.string.error_unknown, listOf("y"))).isNotEqualTo(UiText.Resource(Res.string.error_unknown, listOf("x")))
    }
}

package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.testing.FakeLanguagePreferences
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo

class LanguageControllerTest {

    @Test
    fun aChosenLanguageIsKeptAndShown() {
        val preferences = FakeLanguagePreferences()
        val controller = LanguageController(preferences)

        controller.setLanguage(AppLanguage.ITALIANO)

        assertThat(controller.language.value).isEqualTo(AppLanguage.ITALIANO)
        assertThat(LanguageController(preferences).language.value).isEqualTo(AppLanguage.ITALIANO)
    }
}

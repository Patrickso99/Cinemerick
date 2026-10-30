package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.testing.FakeLanguagePreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageControllerTest {

    @Test
    fun aChosenLanguageIsKeptAndShown() {
        val preferences = FakeLanguagePreferences()
        val controller = LanguageController(preferences)

        controller.setLanguage(AppLanguage.ITALIANO)

        assertEquals(AppLanguage.ITALIANO, controller.language.value)
        assertEquals(AppLanguage.ITALIANO, LanguageController(preferences).language.value)
    }
}

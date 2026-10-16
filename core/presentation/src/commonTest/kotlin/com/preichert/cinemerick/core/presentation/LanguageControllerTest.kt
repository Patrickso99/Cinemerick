package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.data.SettingsLanguagePreferences
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.testing.MemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageControllerTest {

    @Test
    fun aChosenLanguageIsKeptAndShown() {
        val settings = MemorySettings()
        val controller = LanguageController(SettingsLanguagePreferences(settings))

        controller.setLanguage(AppLanguage.ITALIANO)

        assertEquals(AppLanguage.ITALIANO, controller.language.value)
        assertEquals(AppLanguage.ITALIANO, LanguageController(SettingsLanguagePreferences(settings)).language.value)
    }
}

package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.LanguagePreferences
import com.russhwolf.settings.Settings

private const val KEY_LANGUAGE = "app_language"

class SettingsLanguagePreferences(private val settings: Settings) : LanguagePreferences {

    override fun language(): AppLanguage? {
        val value = settings.getStringOrNull(KEY_LANGUAGE) ?: return null
        return try {
            AppLanguage.valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun setLanguage(language: AppLanguage) {
        settings.putString(KEY_LANGUAGE, language.name)
    }
}

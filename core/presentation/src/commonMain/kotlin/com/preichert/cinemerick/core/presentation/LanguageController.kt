package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Stable
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.LanguagePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The language the app is shown in. Without a saved choice it follows the system language (English unless the
 * system is Italian). A saved choice is applied at start and whenever the user changes it.
 */
@Stable
class LanguageController(
    private val preferences: LanguagePreferences,
) {
    // Read before any language is applied, so the launch language is the system one.
    private val systemLanguage = systemAppLanguage()
    private val _language = MutableStateFlow(preferences.language() ?: systemLanguage)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        preferences.language()?.let(::applyAppLanguage)
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language
        preferences.setLanguage(language)
        applyAppLanguage(language)
    }
}

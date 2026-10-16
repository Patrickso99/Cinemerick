package com.preichert.cinemerick.core.domain

interface LanguagePreferences {
    /** The language the user chose, or null if they never chose one (then the system language is used). */
    fun language(): AppLanguage?
    fun setLanguage(language: AppLanguage)
}

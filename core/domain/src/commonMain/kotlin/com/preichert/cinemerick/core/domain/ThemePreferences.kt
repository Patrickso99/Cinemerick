package com.preichert.cinemerick.core.domain

/** Persists the user's light or dark theme. Null means follow the system. */
interface ThemePreferences {
    fun darkTheme(): Boolean?
    fun setDarkTheme(dark: Boolean)
}

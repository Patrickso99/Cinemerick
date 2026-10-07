package com.preichert.cinemerick.core.domain

interface ThemePreferences {
    fun themeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
}
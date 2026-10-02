package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.lifecycle.ViewModel
import com.preichert.cinemerick.core.domain.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _darkTheme = MutableStateFlow(themePreferences.darkTheme())
    val darkTheme = _darkTheme.asStateFlow()

    fun setDarkTheme(dark: Boolean) {
        themePreferences.setDarkTheme(dark)
        _darkTheme.value = dark
    }
}

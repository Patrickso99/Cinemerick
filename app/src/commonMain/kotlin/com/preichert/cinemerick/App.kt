package com.preichert.cinemerick

import androidx.compose.runtime.Composable
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.feature.showtimes.presentation.ShowtimesRoot

@Composable
fun App() {
    CinemerickTheme {
        ShowtimesRoot()
    }
}

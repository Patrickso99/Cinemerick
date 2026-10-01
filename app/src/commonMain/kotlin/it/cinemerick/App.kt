package it.cinemerick

import androidx.compose.runtime.Composable
import it.cinemerick.core.designsystem.CinemerickTheme
import it.cinemerick.feature.showtimes.presentation.ShowtimesRoot

@Composable
fun App() {
    CinemerickTheme {
        ShowtimesRoot()
    }
}

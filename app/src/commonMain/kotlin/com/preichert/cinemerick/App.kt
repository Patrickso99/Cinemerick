package com.preichert.cinemerick

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.feature.showtimes.presentation.ShowtimesRoot

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }
    CinemerickTheme {
        ShowtimesRoot()
    }
}

package com.preichert.cinemerick

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.navigation.NavigationRoot
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.ShowtimesGraphRoutes

@Composable
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }

    val navController = rememberNavController()

    CinemerickTheme(isDarkTheme = isDarkTheme) {
        NavigationRoot(
            navController = navController,
            startDestination = ShowtimesGraphRoutes.Graph
        )
    }
}

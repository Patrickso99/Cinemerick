package com.preichert.cinemerick

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.presentation.AnimationController
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import androidx.compose.runtime.CompositionLocalProvider
import com.preichert.cinemerick.navigation.NavigationRoot
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.ShowtimesGraphRoutes
import org.koin.compose.koinInject

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }

    val themeController: ThemeController = koinInject()
    val themeMode by themeController.mode.collectAsStateWithLifecycle()

    val animationController: AnimationController = koinInject()
    val animationsEnabled by animationController.isEnabled.collectAsStateWithLifecycle()

    val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val navController = rememberNavController()

    CinemerickTheme(isDarkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalAnimationsEnabled provides animationsEnabled) {
            NavigationRoot(
                navController = navController,
                startDestination = ShowtimesGraphRoutes.Graph
            )
        }
    }
}

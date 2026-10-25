package com.preichert.cinemerick

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
import com.preichert.cinemerick.core.presentation.LanguageController
import androidx.compose.runtime.key
import com.preichert.cinemerick.core.presentation.ThemeController
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import androidx.compose.runtime.CompositionLocalProvider
import com.preichert.cinemerick.navigation.NavigationRoot
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.ShowtimesGraphRoutes
import org.koin.compose.koinInject
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.preichert.cinemerick.core.designsystem.CinemerickSplash
import com.preichert.cinemerick.core.domain.BuildKonfig

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }

    val themeController: ThemeController = koinInject()
    val themeMode by themeController.mode.collectAsStateWithLifecycle()

    val animationController: AnimationController = koinInject()
    val animationsEnabled by animationController.isEnabled.collectAsStateWithLifecycle()

    val languageController: LanguageController = koinInject()
    val language by languageController.language.collectAsStateWithLifecycle()

    val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val navController = rememberNavController()
    var splashFinished by rememberSaveable { mutableStateOf(false) }

    CinemerickTheme(isDarkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalAnimationsEnabled provides animationsEnabled) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Keyed on the language: the whole UI is composed again when the user changes it, so every string follows.
                if (splashFinished) {
                    key(language) {
                        NavigationRoot(
                            navController = navController,
                            startDestination = ShowtimesGraphRoutes.Graph
                        )
                    }
                } else {
                    CinemerickSplash(
                        appName = BuildKonfig.APP_NAME,
                        onFinished = { splashFinished = true },
                    )
                }
            }
        }
    }
}

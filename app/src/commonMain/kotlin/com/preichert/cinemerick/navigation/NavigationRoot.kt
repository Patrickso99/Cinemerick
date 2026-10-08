package com.preichert.cinemerick.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import com.preichert.cinemerick.core.designsystem.Motion
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.preichert.cinemerick.feature.settings.presentation.navigation.SettingsGraphRoutes
import com.preichert.cinemerick.feature.settings.presentation.navigation.settingsGraph
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.showtimesGraph

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val slide = tween<androidx.compose.ui.unit.IntOffset>(Motion.Medium, easing = Motion.Easing)
    val fade = tween<Float>(Motion.Medium, easing = Motion.Easing)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            if (animationsEnabled) slideInHorizontally(slide) { it / 4 } + fadeIn(fade) else EnterTransition.None
        },
        exitTransition = {
            if (animationsEnabled) slideOutHorizontally(slide) { -it / 6 } + fadeOut(fade) else ExitTransition.None
        },
        popEnterTransition = {
            if (animationsEnabled) slideInHorizontally(slide) { -it / 6 } + fadeIn(fade) else EnterTransition.None
        },
        popExitTransition = {
            if (animationsEnabled) slideOutHorizontally(slide) { it / 4 } + fadeOut(fade) else ExitTransition.None
        }
    ) {
        showtimesGraph(
            onNavigateToSettings = {
                // Ignore taps during a transition to avoid duplicate navigations on desktop
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.navigate(SettingsGraphRoutes.SettingsRoute) { launchSingleTop = true }
                }
            }
        )
        settingsGraph(navController = navController)
    }
}
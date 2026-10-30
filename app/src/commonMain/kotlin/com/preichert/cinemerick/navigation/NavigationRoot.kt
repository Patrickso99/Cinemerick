package com.preichert.cinemerick.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import com.preichert.cinemerick.core.designsystem.Motion
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.preichert.cinemerick.feature.settings.presentation.navigation.navigateToSettings
import com.preichert.cinemerick.feature.settings.presentation.navigation.settingsGraph
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.showtimesGraph

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val slide = tween<androidx.compose.ui.unit.IntOffset>(Motion.Sheet, easing = Motion.Emphasized)
    val fade = tween<Float>(Motion.Sheet, easing = Motion.Decelerate)
    val scale = tween<Float>(Motion.Sheet, easing = Motion.Emphasized)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            if (animationsEnabled) slideInHorizontally(slide) { it / 4 } + fadeIn(fade) + scaleIn(scale, initialScale = 0.96f) else EnterTransition.None
        },
        exitTransition = {
            if (animationsEnabled) slideOutHorizontally(slide) { -it / 6 } + fadeOut(fade) + scaleOut(scale, targetScale = 0.96f) else ExitTransition.None
        },
        popEnterTransition = {
            if (animationsEnabled) slideInHorizontally(slide) { -it / 6 } + fadeIn(fade) + scaleIn(scale, initialScale = 0.96f) else EnterTransition.None
        },
        popExitTransition = {
            if (animationsEnabled) slideOutHorizontally(slide) { it / 4 } + fadeOut(fade) + scaleOut(scale, targetScale = 0.96f) else ExitTransition.None
        }
    ) {
        showtimesGraph(onNavigateToSettings = navController::navigateToSettings)
        settingsGraph(navController = navController)
    }
}
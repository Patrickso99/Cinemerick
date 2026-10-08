package com.preichert.cinemerick.navigation

import androidx.compose.runtime.Composable
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
    NavHost(
        navController = navController,
        startDestination = startDestination
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
package com.preichert.cinemerick.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.ShowtimesGraphRoutes
import com.preichert.cinemerick.feature.showtimes.presentation.navigation.showtimesGraph
import com.preichert.cinemerick.feature.settings.presentation.navigation.settingsGraph

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        showtimesGraph(navController = navController)
        settingsGraph(navController = navController)
    }
}
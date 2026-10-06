package com.preichert.cinemerick.feature.showtimes.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.preichert.cinemerick.feature.showtimes.presentation.ShowtimesScreenContent
import kotlinx.serialization.Serializable

sealed interface ShowtimesGraphRoutes {
    @Serializable
    data object Graph : ShowtimesGraphRoutes

    @Serializable
    data object ShowtimesRoute : ShowtimesGraphRoutes
}

fun NavGraphBuilder.showtimesGraph(
    navController: NavController
) {
    navigation<ShowtimesGraphRoutes.Graph>(
        startDestination = ShowtimesGraphRoutes.ShowtimesRoute
    ) {
        composable<ShowtimesGraphRoutes.ShowtimesRoute> {
            ShowtimesScreenContent(navController = navController)
        }
    }
}
package com.preichert.cinemerick.feature.settings.presentation.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.preichert.cinemerick.feature.settings.presentation.SettingsScreenContent
import kotlinx.serialization.Serializable

sealed interface SettingsGraphRoutes {
    @Serializable
    data object Graph : SettingsGraphRoutes

    @Serializable
    data object SettingsRoute : SettingsGraphRoutes
}

fun NavGraphBuilder.settingsGraph(
    navController: NavController
) {
    navigation<SettingsGraphRoutes.Graph>(
        startDestination = SettingsGraphRoutes.SettingsRoute
    ) {
        composable<SettingsGraphRoutes.SettingsRoute> {
            SettingsScreenContent(
                onNavigateBack = {
                    // Ignore taps during a transition: a second pop would remove the start destination on desktop
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED && navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
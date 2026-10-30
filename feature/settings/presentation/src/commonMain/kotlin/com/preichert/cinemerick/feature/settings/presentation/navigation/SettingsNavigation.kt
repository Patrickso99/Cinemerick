package com.preichert.cinemerick.feature.settings.presentation.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.preichert.cinemerick.feature.settings.presentation.SettingsScreenContent
import kotlinx.serialization.Serializable

internal sealed interface SettingsGraphRoutes {
    @Serializable
    data object Graph : SettingsGraphRoutes

    @Serializable
    data object SettingsRoute : SettingsGraphRoutes
}

// Ignore taps during a transition: a second navigation would stack or remove destinations on desktop.
private fun NavController.isIdle(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

fun NavController.navigateToSettings() {
    if (isIdle()) navigate(SettingsGraphRoutes.SettingsRoute) { launchSingleTop = true }
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
                    // A second pop would remove the start destination on desktop
                    if (navController.isIdle() && navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

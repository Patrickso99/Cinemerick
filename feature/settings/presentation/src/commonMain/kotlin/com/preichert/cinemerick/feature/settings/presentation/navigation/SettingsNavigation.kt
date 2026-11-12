package com.preichert.cinemerick.feature.settings.presentation.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.preichert.cinemerick.feature.settings.presentation.SettingsRoot
import com.preichert.cinemerick.feature.settings.presentation.key.AssistantKeyRoot
import kotlinx.serialization.Serializable

internal sealed interface SettingsGraphRoutes {
    @Serializable
    data object Graph : SettingsGraphRoutes

    @Serializable
    data object SettingsRoute : SettingsGraphRoutes

    @Serializable
    data object AssistantKeyRoute : SettingsGraphRoutes
}

// Ignore taps during a transition: a second navigation would stack or remove destinations on desktop.
private fun NavController.isIdle(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

// A second pop would remove the start destination on desktop.
private fun NavController.navigateBackSafely() {
    if (isIdle() && previousBackStackEntry != null) popBackStack()
}

fun NavController.navigateToSettings() {
    if (isIdle()) navigate(SettingsGraphRoutes.SettingsRoute) { launchSingleTop = true }
}

/** The API key screen of the assistant, from Settings or from the voice sheet. */
fun NavController.navigateToAssistantKey() {
    if (isIdle()) navigate(SettingsGraphRoutes.AssistantKeyRoute) { launchSingleTop = true }
}

fun NavGraphBuilder.settingsGraph(
    navController: NavController
) {
    navigation<SettingsGraphRoutes.Graph>(
        startDestination = SettingsGraphRoutes.SettingsRoute
    ) {
        composable<SettingsGraphRoutes.SettingsRoute> {
            SettingsRoot(
                onNavigateBack = navController::navigateBackSafely,
                onAssistantKeyClick = navController::navigateToAssistantKey,
            )
        }
        composable<SettingsGraphRoutes.AssistantKeyRoute> {
            AssistantKeyRoot(onNavigateBack = navController::navigateBackSafely)
        }
    }
}

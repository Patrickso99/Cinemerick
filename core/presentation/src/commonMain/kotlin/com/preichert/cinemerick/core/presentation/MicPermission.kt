package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

enum class MicPermissionStatus {
    GRANTED,

    /** Not granted yet: asking shows the system dialog. */
    NOT_GRANTED,

    /** Refused for good (or restricted): only the system settings can grant it now. */
    BLOCKED,
}

/** The microphone permission, read and requested from the screen that needs it. */
@Stable
interface MicPermissionController {
    fun status(): MicPermissionStatus

    /** Shows the system dialog when the system still allows it; [onResult] gets the status afterwards. */
    fun request(onResult: (MicPermissionStatus) -> Unit)

    /** Opens the app's page in the system settings, where a blocked permission can be turned on. */
    fun openSettings()
}

@Composable
expect fun rememberMicPermissionController(): MicPermissionController

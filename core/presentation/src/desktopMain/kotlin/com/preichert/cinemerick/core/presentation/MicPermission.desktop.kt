package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Composable

/** Desktop has no speech input: the assistant is typed, so the microphone is never asked for. */
@Composable
actual fun rememberMicPermissionController(): MicPermissionController = DesktopMicPermissionController

private object DesktopMicPermissionController : MicPermissionController {
    override fun status(): MicPermissionStatus = MicPermissionStatus.BLOCKED
    override fun request(onResult: (MicPermissionStatus) -> Unit) = onResult(MicPermissionStatus.BLOCKED)
    override fun openSettings() = Unit
}

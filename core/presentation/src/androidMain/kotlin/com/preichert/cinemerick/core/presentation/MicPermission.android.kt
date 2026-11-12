package com.preichert.cinemerick.core.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberMicPermissionController(): MicPermissionController {
    val context = LocalContext.current
    // The callback of the request in flight: the launcher result arrives later, outside the call.
    val callback = remember { arrayOfNulls<(MicPermissionStatus) -> Unit>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Refused without a rationale to show means "don't ask again": the dialog will not come back.
        val status = when {
            granted -> MicPermissionStatus.GRANTED
            context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == true -> MicPermissionStatus.NOT_GRANTED
            else -> MicPermissionStatus.BLOCKED
        }
        callback[0]?.invoke(status)
        callback[0] = null
    }
    val currentLauncher by rememberUpdatedState(launcher)
    return remember(context) {
        object : MicPermissionController {
            override fun status(): MicPermissionStatus =
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    MicPermissionStatus.GRANTED
                } else {
                    MicPermissionStatus.NOT_GRANTED
                }

            override fun request(onResult: (MicPermissionStatus) -> Unit) {
                callback[0] = onResult
                currentLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            override fun openSettings() {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

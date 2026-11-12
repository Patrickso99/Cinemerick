package com.preichert.cinemerick.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberMicPermissionController(): MicPermissionController = remember { IosMicPermissionController }

private object IosMicPermissionController : MicPermissionController {
    override fun status(): MicPermissionStatus = when (AVAudioSession.sharedInstance().recordPermission) {
        AVAudioSessionRecordPermissionGranted -> MicPermissionStatus.GRANTED
        AVAudioSessionRecordPermissionDenied -> MicPermissionStatus.BLOCKED
        else -> MicPermissionStatus.NOT_GRANTED
    }

    // iOS asks only once; afterwards the answer is final until changed in Settings.
    override fun request(onResult: (MicPermissionStatus) -> Unit) {
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            dispatch_async(dispatch_get_main_queue()) {
                onResult(if (granted) MicPermissionStatus.GRANTED else MicPermissionStatus.BLOCKED)
            }
        }
    }

    override fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

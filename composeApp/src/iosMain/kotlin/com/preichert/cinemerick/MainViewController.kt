package com.preichert.cinemerick

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController({ initKoin() }) {
        App()
    }
}

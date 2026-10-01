package it.cinemerick

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) { App() }

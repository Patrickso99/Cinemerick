package com.preichert.cinemerick.convention

import org.gradle.api.Project

internal fun Project.composeDesktopCurrentOs(): String {
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    val osId = when {
        "mac" in os -> "macos"
        "win" in os -> "windows"
        else -> "linux"
    }
    val archId = if (arch == "aarch64" || arch == "arm64") "arm64" else "x64"
    return "org.jetbrains.compose.desktop:desktop-jvm-$osId-$archId:${libs.versions.compose.asProvider().get()}"
}

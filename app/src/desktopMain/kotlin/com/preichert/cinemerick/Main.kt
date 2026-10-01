package com.preichert.cinemerick

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Taskbar
import javax.imageio.ImageIO

fun main() {
    System.setProperty("apple.awt.application.name", "Cinemerick")
    setDockIcon()
    initKoin()
    application {
        val icon = BitmapPainter(useResource("icon.png", ::loadImageBitmap))
        Window(onCloseRequest = ::exitApplication, title = "Cinemerick", icon = icon) {
            App()
        }
    }
}

// Window(icon = ...) doesn't change the macOS Dock icon, which otherwise stays the Java Duke when run unpackaged.
private fun setDockIcon() {
    try {
        if (!Taskbar.isTaskbarSupported()) return
        val taskbar = Taskbar.getTaskbar()
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return
        val image = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")?.use(ImageIO::read) ?: return
        taskbar.iconImage = image
    } catch (_: UnsupportedOperationException) {
    } catch (_: SecurityException) {
    }
}

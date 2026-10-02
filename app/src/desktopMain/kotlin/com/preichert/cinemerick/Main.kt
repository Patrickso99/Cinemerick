package com.preichert.cinemerick

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Taskbar
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun main() {
    System.setProperty("apple.awt.application.name", "Cinemerick")
    val icon = loadIcon()
    setDockIcon(icon)
    initKoin()
    application {
        val painter = icon?.let { BitmapPainter(it.toComposeImageBitmap()) }
        Window(onCloseRequest = ::exitApplication, title = "Cinemerick", icon = painter) {
            App()
        }
    }
}

private fun loadIcon(): BufferedImage? =
    Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")?.use(ImageIO::read)

// Window(icon = ...) doesn't change the macOS Dock icon, which otherwise stays the Java Duke when run unpackaged.
private fun setDockIcon(icon: BufferedImage?) {
    try {
        if (icon == null || !Taskbar.isTaskbarSupported()) return
        val taskbar = Taskbar.getTaskbar()
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return
        taskbar.iconImage = icon
    } catch (_: UnsupportedOperationException) {
    } catch (_: SecurityException) {
    }
}

package com.preichert.cinemerick.convention

import java.text.SimpleDateFormat
import java.util.Date

/** Single source of truth for app name, version and code: all platforms read from here. */
object AppInfo {
    const val NAME = "Cinemerick"
    const val VERSION_NAME = "1.1.10"
    val VERSION_CODE: Int = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
}

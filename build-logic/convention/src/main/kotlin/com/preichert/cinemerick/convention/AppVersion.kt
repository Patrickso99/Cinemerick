package com.preichert.cinemerick.convention

import java.text.SimpleDateFormat
import java.util.Date

/** Single source of truth for the app version: Android, desktop packages and BuildKonfig all read from here. */
object AppVersion {
    const val NAME = "1.0.7"
    val CODE: Int = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
}

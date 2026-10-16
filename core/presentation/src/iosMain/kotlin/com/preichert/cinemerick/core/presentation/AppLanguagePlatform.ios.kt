package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.languageCode
import platform.Foundation.currentLocale

actual fun systemAppLanguage(): AppLanguage =
    if (NSLocale.currentLocale.languageCode == "it") AppLanguage.ITALIANO else AppLanguage.ENGLISH

actual fun applyAppLanguage(language: AppLanguage) {
    // iOS reads AppleLanguages when the app starts, so the new language shows after a restart.
    NSUserDefaults.standardUserDefaults.setObject(listOf(language.tag), forKey = "AppleLanguages")
}

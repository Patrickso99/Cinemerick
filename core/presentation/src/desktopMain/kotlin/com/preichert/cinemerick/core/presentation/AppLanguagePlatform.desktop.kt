package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.AppLanguage
import java.util.Locale

private val launchLocale: Locale = Locale.getDefault()

actual fun systemAppLanguage(): AppLanguage =
    if (launchLocale.language == "it") AppLanguage.ITALIANO else AppLanguage.ENGLISH

actual fun applyAppLanguage(language: AppLanguage) {
    Locale.setDefault(Locale.forLanguageTag(language.tag))
}

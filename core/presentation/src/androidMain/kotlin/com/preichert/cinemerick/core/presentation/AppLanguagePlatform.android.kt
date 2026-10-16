package com.preichert.cinemerick.core.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.preichert.cinemerick.core.domain.AppLanguage
import java.util.Locale

actual fun systemAppLanguage(): AppLanguage =
    if (Locale.getDefault().language == "it") AppLanguage.ITALIANO else AppLanguage.ENGLISH

/** Per-app locale: AppCompat applies it to the activities and recreates them, so the strings change at once. */
actual fun applyAppLanguage(language: AppLanguage) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
}

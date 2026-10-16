package com.preichert.cinemerick.core.presentation

import com.preichert.cinemerick.core.domain.AppLanguage

/** The language of the system, mapped to one the app supports. */
expect fun systemAppLanguage(): AppLanguage

/**
 * Makes [language] the language of the app's string resources. Takes effect at once on desktop, and after a restart
 * on iOS. Android does not switch yet: it needs per-app locales.
 */
expect fun applyAppLanguage(language: AppLanguage)

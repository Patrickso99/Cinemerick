package com.preichert.cinemerick.feature.showtimes.presentation

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localizedStringForLanguageCode

internal actual fun languageDisplayName(code: String): String? {
    val tag = code.trim().lowercase()
    if (tag.isEmpty()) return null
    return NSLocale.currentLocale.localizedStringForLanguageCode(tag)
        ?.takeIf { it.isNotBlank() && !it.equals(tag, ignoreCase = true) }
        ?.replaceFirstChar { it.uppercase() }
}

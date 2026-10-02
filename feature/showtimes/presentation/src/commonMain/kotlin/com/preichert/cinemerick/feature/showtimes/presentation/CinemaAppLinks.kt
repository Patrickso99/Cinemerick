package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.AppPlatform
import com.preichert.cinemerick.feature.showtimes.domain.Cinema

data class CinemaAppLink(val label: String, val url: String)

// Android opens the Play Store, iOS the App Store, desktop the cinema website.
// More than one link means the user has to pick (UCI has two Android apps).
fun Cinema.appLinks(platform: AppPlatform): List<CinemaAppLink> = when (this) {
    Cinema.THE_SPACE -> when (platform) {
        AppPlatform.ANDROID -> listOf(CinemaAppLink("The Space Cinema", "https://play.google.com/store/apps/details?id=it.thespacecinema.android"))
        AppPlatform.IOS -> listOf(CinemaAppLink("The Space Cinema", "https://apps.apple.com/it/app/the-space-cinema/id390683419"))
        AppPlatform.DESKTOP -> listOf(CinemaAppLink("The Space Cinema", "https://www.thespacecinema.it/cinema/silea/al-cinema"))
    }
    Cinema.UCI -> when (platform) {
        AppPlatform.ANDROID -> listOf(
            CinemaAppLink("UCI Cinemas (New)", "https://play.google.com/store/apps/details?id=it.ucicinemas.app"),
            CinemaAppLink("UCI Cinemas (Old)", "https://play.google.com/store/apps/details?id=it.creaweb.ucicinemas&hl=it")
        )
        AppPlatform.IOS -> listOf(CinemaAppLink("UCI Cinemas", "https://apps.apple.com/it/app/uci-cinemas-italia/id6746633899"))
        AppPlatform.DESKTOP -> listOf(CinemaAppLink("UCI Cinemas", "https://ucicinemas.it/cinema/uci-cinemas-venezia-marcon"))
    }
    Cinema.NOTORIOUS -> when (platform) {
        AppPlatform.ANDROID -> listOf(CinemaAppLink("Notorious Cinemas", "https://play.google.com/store/apps/details?id=it.creaweb.notorious"))
        AppPlatform.IOS -> listOf(CinemaAppLink("Notorious Cinemas", "https://apps.apple.com/it/app/notorious-cinemas-webtic/id1450928428"))
        AppPlatform.DESKTOP -> listOf(CinemaAppLink("Notorious Cinemas", "https://www.notoriouscinemas.it/ferrara/index.php"))
    }
    Cinema.CINERGIA -> listOf(CinemaAppLink("Cinergia Conegliano", "https://coneglianocinergia.18tickets.it"))
    Cinema.CRISTALLO -> listOf(CinemaAppLink("Cinema Cristallo", "https://www.cinemacristallo.com"))
}

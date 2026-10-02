package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.AppPlatform
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue

data class CinemaAppLink(val label: String, val url: String)

fun Venue.appLinks(platform: AppPlatform): List<CinemaAppLink> =
    when (platform) {
        AppPlatform.ANDROID -> chain.androidStoreLinks()
        AppPlatform.IOS -> chain.iosStoreLinks()
        AppPlatform.DESKTOP -> listOfNotNull(
            webUrl?.let { CinemaAppLink(name, it) }
                ?: CinemaAppLink(name, chain.defaultWebUrl())
        )
    }

private fun Chain.androidStoreLinks() = when (this) {
    Chain.THE_SPACE -> listOf(CinemaAppLink("The Space Cinema", "https://play.google.com/store/apps/details?id=it.thespacecinema.android"))
    Chain.UCI -> listOf(
        CinemaAppLink("UCI Cinemas (New)", "https://play.google.com/store/apps/details?id=it.ucicinemas.app"),
        CinemaAppLink("UCI Cinemas (Old)", "https://play.google.com/store/apps/details?id=it.creaweb.ucicinemas&hl=it")
    )
    Chain.NOTORIOUS -> listOf(CinemaAppLink("Notorious Cinemas", "https://play.google.com/store/apps/details?id=it.creaweb.notorious"))
    Chain.CINERGIA -> listOf(CinemaAppLink("Cinergia", "https://play.google.com/store/apps/details?id=com.eighteentickets"))
    Chain.CRISTALLO -> listOf(CinemaAppLink("Cinema Cristallo", "https://play.google.com/store/apps/details?id=com.cinemacristallo"))
}

private fun Chain.iosStoreLinks() = when (this) {
    Chain.THE_SPACE -> listOf(CinemaAppLink("The Space Cinema", "https://apps.apple.com/it/app/the-space-cinema/id390683419"))
    Chain.UCI -> listOf(CinemaAppLink("UCI Cinemas", "https://apps.apple.com/it/app/uci-cinemas-italia/id6746633899"))
    Chain.NOTORIOUS -> listOf(CinemaAppLink("Notorious Cinemas", "https://apps.apple.com/it/app/notorious-cinemas-webtic/id1450928428"))
    Chain.CINERGIA -> listOf(CinemaAppLink("Cinergia", "https://apps.apple.com/it/app/18tickets/id1234567890"))
    Chain.CRISTALLO -> listOf(CinemaAppLink("Cinema Cristallo", "https://apps.apple.com/it/app/cinema-cristallo/id9876543210"))
}

private fun Chain.defaultWebUrl() = when (this) {
    Chain.THE_SPACE -> "https://www.thespacecinema.it"
    Chain.UCI -> "https://ucicinemas.it"
    Chain.NOTORIOUS -> "https://www.notoriouscinemas.it"
    Chain.CINERGIA -> "https://coneglianocinergia.18tickets.it"
    Chain.CRISTALLO -> "https://www.cinemacristallo.com"
}

package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import kotlinx.datetime.LocalDate

internal object PreviewData {
    val sileaVenue = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
    val marconVenue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
    val starliteVenue = Venue(Chain.NOTORIOUS, "notorious-001", "StarLite", "Lazio")

    fun emptyShowtimesState() = ShowtimesState()

    fun loadingShowtimesState() = ShowtimesState(isLoading = true)

    fun showtimesStateWithVenues() = ShowtimesState(
        selectedVenues = setOf(sileaVenue, marconVenue)
    )

    fun showtimesStateWithDays() = ShowtimesState(
        days = listOf(
            DayUi(LocalDate(2026, 10, 1), "gio 01/10", isSelected = true),
            DayUi(LocalDate(2026, 10, 2), "ven 02/10", isSelected = true),
            DayUi(LocalDate(2026, 10, 3), "sab 03/10"),
            DayUi(LocalDate(2026, 10, 4), "dom 04/10")
        ),
        selectedVenues = setOf(sileaVenue, marconVenue)
    )

    fun showtimesStateWithResults() = ShowtimesState(
        days = listOf(
            DayUi(LocalDate(2026, 10, 1), "gio 01/10", isSelected = true),
            DayUi(LocalDate(2026, 10, 2), "ven 02/10", isSelected = true, maxTime = "24"),
            DayUi(LocalDate(2026, 10, 3), "sab 03/10", isSelected = true, minTime = "25"),
            DayUi(LocalDate(2026, 10, 4), "dom 04/10")
        ),
        selectedVenues = setOf(sileaVenue, marconVenue),
        hasGenerated = true,
        pollText = "Film Name (Giovedì - 21:00 - Silea)",
        optionsCount = 2,
        groups = listOf(
            FilmGroupUi(
                title = "Film Name",
                posterUrl = null,
                showings = listOf(
                    ShowingUi("Giovedì", "01/10", "21:00", sileaVenue, "2D · VO"),
                    ShowingUi("Giovedì", "01/10", "21:30", marconVenue, "2D · ENG · sub ITA")
                )
            )
        )
    )

    fun showtimesStateWithErrors() = ShowtimesState(
        selectedVenues = setOf(sileaVenue, marconVenue),
        venueErrors = listOf(
            VenueErrorUi(
                venueName = "The Space Silea",
                message = UiText.DynamicString("Connection timeout")
            ),
            VenueErrorUi(
                venueName = "UCI Marcon",
                message = UiText.DynamicString("Cinema website is down")
            )
        )
    )

    fun filtersStateWithFormats() = ShowtimesState(
        hasGenerated = true,
        availableTags = listOf("2D", "3D", "VO", "sub ITA", "4DX", "IMAX"),
        selectedVenues = setOf(sileaVenue, marconVenue)
    )

    fun dayUiSelected() = DayUi(
        LocalDate(2026, 10, 1),
        "gio 01/10",
        isSelected = true
    )

    fun dayUiNotSelected() = DayUi(
        LocalDate(2026, 10, 2),
        "ven 02/10",
        isSelected = false
    )

    fun dayUiWithTimeError() = DayUi(
        LocalDate(2026, 10, 3),
        "sab 03/10",
        isSelected = true,
        minTime = "25",
        maxTime = "24"
    )

    fun filmGroupWithPoster() = FilmGroupUi(
        title = "Dune: Part Two",
        posterUrl = "https://example.com/poster.jpg",
        showings = listOf(
            ShowingUi("Giovedì", "01/10", "19:00", sileaVenue, "3D · VO"),
            ShowingUi("Giovedì", "01/10", "21:30", sileaVenue, "2D · VO")
        )
    )

    fun filmGroupNoPoster() = FilmGroupUi(
        title = "Inception",
        posterUrl = null,
        showings = listOf(
            ShowingUi("Venerdì", "02/10", "20:00", marconVenue, "2D · ITA")
        )
    )

    fun venueCatalogLoading() = VenuePickerUi(
        chain = Chain.THE_SPACE,
        catalog = VenueCatalogUi.Loading
    )

    fun venueCatalogLoaded() = VenuePickerUi(
        chain = Chain.THE_SPACE,
        catalog = VenueCatalogUi.Loaded(
            venues = listOf(
                Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto"),
                Venue(Chain.THE_SPACE, "1005", "Pordenone", "Friuli-Venezia Giulia"),
                Venue(Chain.THE_SPACE, "1014", "Udine", "Friuli-Venezia Giulia")
            )
        )
    )

    fun venueCatalogEmpty() = VenuePickerUi(
        chain = Chain.THE_SPACE,
        query = "xxxxxx",
        catalog = VenueCatalogUi.Loaded(venues = emptyList())
    )

    fun venueCatalogError() = VenuePickerUi(
        chain = Chain.THE_SPACE,
        catalog = VenueCatalogUi.Error(
            message = UiText.DynamicString("Failed to load venues. Please try again.")
        )
    )
}

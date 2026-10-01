package it.cinemerick.feature.showtimes.data

import kotlinx.serialization.Serializable

@Serializable
data class SpaceFilmsDto(
    val result: List<SpaceFilmDto> = emptyList()
)

@Serializable
data class SpaceFilmDto(
    val filmTitle: String = "",
    val showingGroups: List<SpaceShowingGroupDto> = emptyList()
)

@Serializable
data class SpaceShowingGroupDto(
    val sessions: List<SpaceSessionDto> = emptyList()
)

@Serializable
data class SpaceSessionDto(
    val startTime: String = ""
)

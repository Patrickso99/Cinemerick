package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.serialization.Serializable

@Serializable
data class TheSpaceCinemasDto(
    val result: List<TheSpaceCinemaGroupDto> = emptyList()
)

@Serializable
data class TheSpaceCinemaGroupDto(
    val alpha: String = "",
    val cinemas: List<TheSpaceCinemaDto> = emptyList()
)

@Serializable
data class TheSpaceCinemaDto(
    val cinemaId: String = "",
    val cinemaName: String = "",
    val fullName: String = "",
    val whatsOnUrl: String? = null
)

@Serializable
data class TheSpaceFilmsDto(
    val result: List<TheSpaceFilmDto> = emptyList()
)

@Serializable
data class TheSpaceFilmDto(
    val filmTitle: String = "",
    val posterImageSrc: String? = null,
    val showingGroups: List<TheSpaceShowingGroupDto> = emptyList()
)

@Serializable
data class TheSpaceShowingGroupDto(
    val sessions: List<TheSpaceSessionDto> = emptyList()
)

@Serializable
data class TheSpaceSessionDto(
    val startTime: String = "",
    val attributes: List<TheSpaceAttributeDto> = emptyList()
)

@Serializable
data class TheSpaceAttributeDto(
    val name: String = "",
    val attributeType: String = ""
)

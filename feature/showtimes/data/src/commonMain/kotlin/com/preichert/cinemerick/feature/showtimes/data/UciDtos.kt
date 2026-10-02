package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UciTheatresDto(
    val data: List<UciTheatreDto> = emptyList()
)

@Serializable
data class UciTheatreDto(
    val id: Int = 0,
    val name: String = "",
    val slug: String = "",
    val city: String = "",
    val province: String = "",
    val region: String = ""
)

@Serializable
data class UciProgrammingDto(
    val data: List<UciMovieDto> = emptyList()
)

@Serializable
data class UciMovieDto(
    val title: String = "",
    val poster: String? = null,
    val screens: List<Map<String, List<UciVariantDto>>> = emptyList()
)

@Serializable
data class UciVariantDto(
    val language: UciNamedDto? = null,
    val subtitles: UciNamedDto? = null,
    val performances: List<UciPerformanceDto> = emptyList()
)

@Serializable
data class UciPerformanceDto(
    val day: String = "",
    @SerialName("actual_start_at") val actualStartAt: String = ""
)

@Serializable
data class UciNamedDto(
    val name: String = ""
)

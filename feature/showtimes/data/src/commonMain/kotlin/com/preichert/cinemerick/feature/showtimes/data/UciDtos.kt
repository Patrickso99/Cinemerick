package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UciProgrammingDto(
    val data: List<UciMovieDto> = emptyList()
)

@Serializable
data class UciMovieDto(
    val title: String = "",
    val screens: List<Map<String, List<UciVariantDto>>> = emptyList()
)

@Serializable
data class UciVariantDto(
    val performances: List<UciPerformanceDto> = emptyList()
)

@Serializable
data class UciPerformanceDto(
    val day: String = "",
    @SerialName("actual_start_at") val actualStartAt: String = ""
)

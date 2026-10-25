package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// TMDB API v3: GET /search/movie, GET /movie/{id}?append_to_response=credits,videos,release_dates,images and
// GET /person/{id}?append_to_response=combined_credits

@Serializable
internal data class TmdbSearchDto(
    val results: List<TmdbSearchResultDto> = emptyList()
)

@Serializable
internal data class TmdbSearchResultDto(
    val id: Int = 0,
    val title: String = "",
    @SerialName("original_title") val originalTitle: String = "",
    // Empty or missing for films without a date.
    @SerialName("release_date") val releaseDate: String? = null,
    val popularity: Double = 0.0
)

@Serializable
internal data class TmdbMovieDto(
    val id: Int = 0,
    val title: String = "",
    @SerialName("original_title") val originalTitle: String = "",
    val overview: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosDto? = null,
    @SerialName("release_dates") val releaseDates: TmdbReleaseDatesDto? = null,
    val tagline: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("belongs_to_collection") val collection: TmdbCollectionDto? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("production_countries") val productionCountries: List<TmdbCountryDto> = emptyList(),
    @SerialName("production_companies") val productionCompanies: List<TmdbCompanyDto> = emptyList(),
    val budget: Long = 0,
    val revenue: Long = 0,
    val images: TmdbImagesDto? = null,
    @SerialName("poster_path") val posterPath: String? = null
)

@Serializable
internal data class TmdbCompanyDto(
    val name: String = ""
)

@Serializable
internal data class TmdbImagesDto(
    val backdrops: List<TmdbImageDto> = emptyList(),
    val logos: List<TmdbImageDto> = emptyList()
)

@Serializable
internal data class TmdbImageDto(
    @SerialName("file_path") val filePath: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    // Null for an image without text.
    @SerialName("iso_639_1") val language: String? = null
)

@Serializable
internal data class TmdbGenreDto(
    val name: String = ""
)

@Serializable
internal data class TmdbCollectionDto(
    val name: String = ""
)

@Serializable
internal data class TmdbCountryDto(
    val name: String = ""
)

@Serializable
internal data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

@Serializable
internal data class TmdbCastDto(
    val name: String = "",
    val id: Int = 0,
    val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null
)

@Serializable
internal data class TmdbCrewDto(
    val name: String = "",
    val job: String = "",
    val id: Int = 0,
    @SerialName("profile_path") val profilePath: String? = null
)

@Serializable
internal data class TmdbVideosDto(
    val results: List<TmdbVideoDto> = emptyList()
)

@Serializable
internal data class TmdbVideoDto(
    val key: String = "",
    val site: String = "",
    val type: String = "",
    val official: Boolean = false,
    @SerialName("iso_639_1") val language: String = ""
)

@Serializable
internal data class TmdbReleaseDatesDto(
    val results: List<TmdbCountryReleasesDto> = emptyList()
)

@Serializable
internal data class TmdbCountryReleasesDto(
    @SerialName("iso_3166_1") val country: String = "",
    @SerialName("release_dates") val releaseDates: List<TmdbReleaseDateDto> = emptyList()
)

@Serializable
internal data class TmdbReleaseDateDto(
    val certification: String = "",
    // 1 premiere, 2 limited theatrical, 3 theatrical, 4 digital, 5 physical, 6 TV.
    val type: Int = 0,
    @SerialName("release_date") val releaseDate: String = "",
    // Kept as raw JSON: only plain strings are used, and an unexpected shape must not fail the whole film.
    val descriptors: List<JsonElement> = emptyList()
)

@Serializable
internal data class TmdbPersonDto(
    val id: Int = 0,
    val name: String = "",
    val biography: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    @SerialName("place_of_birth") val placeOfBirth: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("combined_credits") val combinedCredits: TmdbCombinedCreditsDto? = null
)

@Serializable
internal data class TmdbCombinedCreditsDto(
    val cast: List<TmdbPersonCreditDto> = emptyList(),
    val crew: List<TmdbPersonCreditDto> = emptyList()
)

// A film or a series: only the films are used.
@Serializable
internal data class TmdbPersonCreditDto(
    val id: Int = 0,
    @SerialName("media_type") val mediaType: String = "",
    val title: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val character: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0
)

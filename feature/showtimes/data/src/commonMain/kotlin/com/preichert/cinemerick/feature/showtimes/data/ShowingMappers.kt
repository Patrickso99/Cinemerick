package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun UciProgrammingDto.toShowings(day: LocalDate): List<Showing> {
    val dayText = day.toString()
    return data.filter { it.title.isNotBlank() }.flatMap { movie ->
        movie.screens
            .flatMap { variantsByFormat -> variantsByFormat.values.flatten() }
            .flatMap { variant -> variant.performances }
            .filter { it.day == dayText && it.actualStartAt.isNotBlank() }
            .map { Showing(movie.title, day, LocalTime.parse(it.actualStartAt), Cinema.MARCON) }
    }
}

fun SpaceFilmsDto.toShowings(day: LocalDate): List<Showing> {
    val dayText = day.toString()
    return result.filter { it.filmTitle.isNotBlank() }.flatMap { film ->
        film.showingGroups
            .flatMap { group -> group.sessions }
            .filter { it.startTime.take(10) == dayText && it.startTime.length >= 16 }
            .map { Showing(film.filmTitle, day, LocalTime.parse(it.startTime.substring(11, 16)), Cinema.SILEA) }
    }
}

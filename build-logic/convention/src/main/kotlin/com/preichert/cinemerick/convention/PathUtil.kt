package com.preichert.cinemerick.convention

import org.gradle.api.Project
import java.util.Locale

const val BASE_PACKAGE = "com.preichert.cinemerick"

fun Project.pathToPackageName(): String {
    val relativePackageName = path
        .replace(':', '.')
        .replace("-", "")
        .lowercase()

    return "$BASE_PACKAGE$relativePackageName"
}

fun Project.pathToFrameworkName(): String {
    val parts = path.split(":", "-", "_", " ")
    return parts.joinToString("") { part ->
        part.replaceFirstChar { char -> char.titlecase(Locale.ROOT) }
    }
}

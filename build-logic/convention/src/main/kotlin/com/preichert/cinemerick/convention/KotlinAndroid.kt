package com.preichert.cinemerick.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(extension: ApplicationExtension) {
    with(extension) {
        compileSdk = versionInt("androidCompileSdk")
        defaultConfig.minSdk = versionInt("androidMinSdk")
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

internal fun Project.configureKotlinAndroid(extension: LibraryExtension) {
    with(extension) {
        compileSdk = versionInt("androidCompileSdk")
        defaultConfig.minSdk = versionInt("androidMinSdk")
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

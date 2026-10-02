package com.preichert.cinemerick.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(extension: ApplicationExtension) {
    with(extension) {
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        defaultConfig.minSdk = libs.versions.androidMinSdk.get().toInt()
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

package com.preichert.cinemerick.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<LibraryExtension> {
        namespace = pathToPackageName()
        configureKotlinAndroid(this)
    }

    configureAndroidTarget()
    configureDesktopTarget()
    configureIosTargets(frameworkName = pathToFrameworkName(), isStatic = false)
    configureWasmTarget()

    dependencies {
        "commonTestImplementation"(libs.findLibrary("kotlin-test").get())
    }
}

package com.preichert.cinemerick.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    configureAndroidTarget(pathToPackageName())
    configureDesktopTarget()
    configureIosTargets(frameworkName = pathToFrameworkName())

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(libs.turbine)
            implementation(libs.coroutines.test)
        }
        // UnconfinedTestDispatcher, Dispatchers.setMain and advanceUntilIdle are still marked experimental.
        sourceSets.matching { it.name.endsWith("Test") }.configureEach {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
}

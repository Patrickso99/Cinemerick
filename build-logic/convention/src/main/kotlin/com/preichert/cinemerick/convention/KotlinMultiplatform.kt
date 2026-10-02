package com.preichert.cinemerick.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    configureAndroidTarget(pathToPackageName())
    configureDesktopTarget()
    configureIosTargets(frameworkName = pathToFrameworkName(), isStatic = false)

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

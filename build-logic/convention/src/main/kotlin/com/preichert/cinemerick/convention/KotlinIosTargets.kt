package com.preichert.cinemerick.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureIosTargets(
    frameworkName: String,
    isStatic: Boolean = true
) {
    extensions.configure<KotlinMultiplatformExtension> {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = frameworkName
                this.isStatic = isStatic
            }
        }
    }
}

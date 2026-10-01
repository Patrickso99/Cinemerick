package com.preichert.cinemerick.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureWasmTarget() {
    extensions.configure<KotlinMultiplatformExtension> {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            if (project.path == ":app") {
                browser { commonWebpackConfig { outputFileName = "composeApp.js" } }
                outputModuleName.set("composeApp")
                binaries.executable()
            }
        }
    }
}

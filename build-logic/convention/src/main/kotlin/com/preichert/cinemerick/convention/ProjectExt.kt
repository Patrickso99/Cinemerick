package com.preichert.cinemerick.convention

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

fun PluginManager.apply(plugin: Provider<PluginDependency>) = apply(plugin.get().pluginId)

internal object ConventionPlugin {
    // Core
    const val KMP_LIBRARY = "com.preichert.convention.kmp.library"
    const val CMP_LIBRARY = "com.preichert.convention.cmp.library"
    const val CMP_APPLICATION = "com.preichert.convention.cmp.application"
    const val ANDROID_APPLICATION = "com.preichert.convention.android.application"

    // Feature
    const val CMP_FEATURE = "com.preichert.convention.cmp.feature"

    // Infrastructure
    const val KOIN = "com.preichert.convention.koin"
    const val KTOR = "com.preichert.convention.ktor"
    const val SERIALIZATION = "com.preichert.convention.serialization"
    const val BUILDKONFIG = "com.preichert.convention.buildkonfig"
}

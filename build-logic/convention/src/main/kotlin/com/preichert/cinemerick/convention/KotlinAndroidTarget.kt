package com.preichert.cinemerick.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureAndroidTarget(packageName: String) {
    val compileSdkVersion = libs.versions.androidCompileSdk.get().toInt()
    val minSdkVersion = libs.versions.androidMinSdk.get().toInt()
    extensions.configure<KotlinMultiplatformExtension> {
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
            namespace = packageName
            compileSdk = compileSdkVersion
            minSdk = minSdkVersion
            compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
            withHostTest { }
            androidResources { enable = true }
        }
    }
}

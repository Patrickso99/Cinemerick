import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.preichert.cinemerick.convention.buildlogic"

dependencies {
    compileOnly(libs.build.android.gradle.plugin)
    compileOnly(libs.build.kotlin.gradle.plugin)
    compileOnly(libs.build.compose.gradle.plugin)
    compileOnly(libs.build.compose.compiler)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "cinemerick.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = "cinemerick.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "cinemerick.kmp.compose"
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "cinemerick.kmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("koin") {
            id = "cinemerick.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("ktor") {
            id = "cinemerick.ktor"
            implementationClass = "KtorConventionPlugin"
        }
        register("serialization") {
            id = "cinemerick.kotlinx-serialization"
            implementationClass = "SerializationConventionPlugin"
        }
    }
}

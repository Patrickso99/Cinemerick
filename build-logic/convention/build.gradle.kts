import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.preichert.convention.buildlogic"

dependencies {
    implementation(libs.build.android.gradle.plugin)
    implementation(libs.build.kotlin.gradle.plugin)
    implementation(libs.build.compose.gradle.plugin)
    implementation(libs.build.compose.compiler)
    implementation(libs.build.kotlin.serialization)
    // BuildKonfig pulls Kotlin 2.4 transitively; the project is pinned to libs.versions.kotlin.
    implementation(libs.build.buildkonfig) {
        exclude(group = "org.jetbrains.kotlin")
    }
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
            id = "com.preichert.convention.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("cmpApplication") {
            id = "com.preichert.convention.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = "com.preichert.convention.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "com.preichert.convention.cmp.library"
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "com.preichert.convention.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("buildKonfig") {
            id = "com.preichert.convention.buildkonfig"
            implementationClass = "BuildKonfigConventionPlugin"
        }
        register("koin") {
            id = "com.preichert.convention.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("ktor") {
            id = "com.preichert.convention.ktor"
            implementationClass = "KtorConventionPlugin"
        }
        register("serialization") {
            id = "com.preichert.convention.serialization"
            implementationClass = "SerializationConventionPlugin"
        }
    }
}

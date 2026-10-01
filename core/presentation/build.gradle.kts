plugins {
    id("cinemerick.kmp.compose")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(lib("androidx-lifecycle-runtime-compose"))
        implementation(lib("coroutines-core"))
        implementation(lib("compose-material3-adaptive"))
        implementation(lib("compose-material3-adaptive-layout"))
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.preichert.cinemerick.core.presentation.resources"
}

plugins {
    id("cinemerick.kmp.compose")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(lib("androidx-lifecycle-runtime-compose"))
        implementation(lib("coroutines-core"))
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "it.cinemerick.core.presentation.resources"
}

plugins {
    id("com.preichert.convention.cmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.coroutines.core)
        implementation(libs.compose.material3.adaptive)
        implementation(libs.compose.material3.adaptive.layout)
        implementation(libs.koin.core)
        implementation(libs.koin.compose)
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.preichert.cinemerick.core.presentation.resources"
}

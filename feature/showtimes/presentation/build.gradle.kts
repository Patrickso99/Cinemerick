plugins {
    id("com.preichert.convention.cmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:presentation"))
        implementation(project(":core:design-system"))
        implementation(project(":feature:showtimes:domain"))
        implementation(project(":feature:settings:presentation"))
        implementation(libs.kermit)
        implementation(libs.coil.compose)
        implementation(libs.compose.navigation)
        implementation(libs.serialization.json)
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.preichert.cinemerick.feature.showtimes.presentation.resources"
}

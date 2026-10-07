plugins {
    id("com.preichert.convention.cmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:presentation"))
        implementation(project(":core:design-system"))
        implementation(libs.compose.navigation)
        implementation(libs.serialization.json)
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.preichert.cinemerick.feature.settings.presentation.resources"
}
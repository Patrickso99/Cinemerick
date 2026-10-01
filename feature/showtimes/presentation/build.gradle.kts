plugins {
    id("cinemerick.kmp.feature")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:presentation"))
        implementation(project(":core:design-system"))
        implementation(project(":feature:showtimes:domain"))
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "it.cinemerick.feature.showtimes.presentation.resources"
}

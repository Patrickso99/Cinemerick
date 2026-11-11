plugins {
    id("com.preichert.convention.cmp.feature")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(projects.core.testing)
    }
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.feature.showtimes.domain)
        implementation(libs.kermit)
        implementation(libs.coil.compose)
    }
}
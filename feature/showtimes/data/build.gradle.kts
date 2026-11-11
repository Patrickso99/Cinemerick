plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.koin")
    id("com.preichert.convention.serialization")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(projects.core.testing)
        implementation(libs.ktor.mock)
    }
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.data)
        implementation(projects.feature.showtimes.domain)
        implementation(libs.coroutines.core)
        implementation(libs.kermit)
        implementation(libs.settings.no.arg)
    }
}

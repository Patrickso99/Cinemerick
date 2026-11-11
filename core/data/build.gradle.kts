plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.ktor")
    id("com.preichert.convention.koin")
    id("com.preichert.convention.serialization")
    id("com.preichert.convention.tmdb.buildkonfig")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(projects.core.testing)
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.settings.no.arg)
        }
    }
}

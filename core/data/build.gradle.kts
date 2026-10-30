plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.ktor")
    id("com.preichert.convention.koin")
    id("com.preichert.convention.serialization")
    id("com.preichert.convention.tmdb.buildkonfig")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(project(":core:testing"))
        implementation(libs.coroutines.test)
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.settings.no.arg)
        }
    }
}

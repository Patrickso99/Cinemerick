plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.koin")
    id("com.preichert.convention.serialization")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:data"))
        implementation(project(":feature:showtimes:domain"))
        implementation(libs.coroutines.core)
        implementation(libs.datetime)
    }
}

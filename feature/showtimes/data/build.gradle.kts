plugins {
    id("cinemerick.kmp.library")
    id("cinemerick.koin")
    id("cinemerick.kotlinx-serialization")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:data"))
        implementation(project(":feature:showtimes:domain"))
        implementation(lib("coroutines-core"))
        implementation(lib("datetime"))
    }
}

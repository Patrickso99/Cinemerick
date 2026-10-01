plugins {
    id("cinemerick.kmp.compose")
    id("cinemerick.koin")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(lib("koin-compose"))
        implementation(lib("koin-compose-viewmodel"))
        implementation(lib("androidx-lifecycle-viewmodel-compose"))
        implementation(lib("androidx-lifecycle-runtime-compose"))
        implementation(lib("coroutines-core"))
        implementation(lib("datetime"))
    }
}

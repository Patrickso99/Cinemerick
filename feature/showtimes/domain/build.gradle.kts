plugins {
    id("cinemerick.kmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:domain"))
        api(lib("datetime"))
    }
}

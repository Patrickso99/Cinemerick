plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.buildkonfig")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.coroutines.core)
    }
}

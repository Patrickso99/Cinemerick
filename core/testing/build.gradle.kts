plugins {
    id("com.preichert.convention.kmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.settings.no.arg)
    }
}

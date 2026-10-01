plugins {
    id("com.preichert.convention.kmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:domain"))
        api(libs.datetime)
    }
}

plugins {
    id("com.preichert.convention.kmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.domain)
        api(libs.datetime)
    }
}

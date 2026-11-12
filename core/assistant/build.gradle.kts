plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.ktor")
    id("com.preichert.convention.koin")
    id("com.preichert.convention.serialization")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(projects.core.testing)
        implementation(libs.ktor.mock)
    }
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.data)
        implementation(libs.coroutines.core)
        implementation(libs.kermit)
        implementation(libs.settings.no.arg)
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.koin.android)
        implementation(libs.mlkit.genai.prompt)
    }
}

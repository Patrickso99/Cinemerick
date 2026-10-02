plugins {
    id("com.preichert.convention.kmp.library")
    id("com.preichert.convention.ktor")
    id("com.preichert.convention.koin")
    id("com.preichert.convention.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.settings.no.arg)
        }
        androidMain.dependencies { implementation(libs.ktor.okhttp) }
        getByName("desktopMain").dependencies { implementation(libs.ktor.okhttp) }
        iosMain.dependencies { implementation(libs.ktor.darwin) }
    }
}

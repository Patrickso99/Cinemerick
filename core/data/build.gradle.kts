plugins {
    id("cinemerick.kmp.library")
    id("cinemerick.ktor")
    id("cinemerick.koin")
    id("cinemerick.kotlinx-serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(lib("coroutines-core"))
        }
        androidMain.dependencies { implementation(lib("ktor-okhttp")) }
        getByName("desktopMain").dependencies { implementation(lib("ktor-okhttp")) }
        iosMain.dependencies { implementation(lib("ktor-darwin")) }
        wasmJsMain.dependencies { implementation(lib("ktor-js")) }
    }
}

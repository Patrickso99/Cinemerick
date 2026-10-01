plugins {
    id("cinemerick.android.application")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:presentation"))
            implementation(project(":core:design-system"))
            implementation(project(":feature:showtimes:domain"))
            implementation(project(":feature:showtimes:data"))
            implementation(project(":feature:showtimes:presentation"))
            implementation(lib("compose-runtime"))
            implementation(lib("compose-foundation"))
            implementation(lib("compose-material3"))
            implementation(lib("compose-ui"))
            implementation(lib("koin-core"))
            implementation(lib("koin-compose"))
        }
        androidMain.dependencies {
            implementation(lib("koin-android"))
            implementation(lib("androidx-activity-compose"))
        }
        getByName("desktopMain").dependencies {
            implementation(lib("coroutines-swing"))
        }
    }
}

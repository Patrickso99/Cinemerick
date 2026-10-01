plugins {
    id("com.preichert.convention.cmp.application")
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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kermit)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.activity.compose)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.coroutines.swing)
        }
    }
}

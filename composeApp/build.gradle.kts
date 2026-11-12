plugins {
    id("com.preichert.convention.cmp.application")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)
            implementation(projects.core.assistant)
            implementation(projects.feature.showtimes.domain)
            implementation(projects.feature.showtimes.data)
            implementation(projects.feature.showtimes.presentation)
            implementation(projects.feature.settings.presentation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.navigation)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kermit)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.coroutines.core)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.coroutines.swing)
        }
    }
}


plugins {
    id("com.preichert.convention.cmp.library")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(project(":core:testing"))
        implementation(libs.coroutines.test)
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.appcompat)
    }
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.coroutines.core)
        implementation(libs.compose.material3.adaptive)
        implementation(libs.compose.material3.adaptive.layout)
        implementation(libs.koin.core)
        implementation(libs.koin.compose)
    }
}
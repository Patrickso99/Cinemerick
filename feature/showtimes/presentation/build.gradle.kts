plugins {
    id("com.preichert.convention.cmp.feature")
    id("com.preichert.convention.serialization")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(project(":core:testing"))
        implementation(project(":core:data"))
        implementation(libs.coroutines.test)
        implementation(libs.assertk)
    }
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:presentation"))
        implementation(project(":core:design-system"))
        implementation(project(":feature:showtimes:domain"))
        implementation(libs.kermit)
        implementation(libs.coil.compose)
        implementation(libs.compose.navigation)
        implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    }
}
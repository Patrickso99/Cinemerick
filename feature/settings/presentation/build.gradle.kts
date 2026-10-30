plugins {
    id("com.preichert.convention.cmp.feature")
    id("com.preichert.convention.serialization")
}

kotlin {
    sourceSets.commonTest.dependencies {
        implementation(project(":core:testing"))
        implementation(libs.coroutines.test)
    }
    sourceSets.commonMain.dependencies {
        implementation(project(":core:domain"))
        implementation(project(":core:presentation"))
        implementation(project(":core:design-system"))
        implementation(libs.compose.navigation)
    }
}
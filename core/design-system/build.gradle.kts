plugins {
    id("com.preichert.convention.cmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.compose.material3.adaptive)
        implementation(libs.compose.material3.adaptive.layout)
    }
}

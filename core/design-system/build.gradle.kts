plugins {
    id("com.preichert.convention.cmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:presentation"))
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.preichert.cinemerick.core.designsystem.resources"
}

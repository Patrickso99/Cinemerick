plugins {
    id("com.preichert.convention.cmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:presentation"))
    }
}

plugins {
    id("cinemerick.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(lib("compose-runtime"))
        implementation(lib("compose-foundation"))
        implementation(lib("compose-material3"))
        implementation(lib("compose-ui"))
        implementation(lib("compose-resources"))
        implementation(lib("compose-ui-tooling-preview"))
    }
}

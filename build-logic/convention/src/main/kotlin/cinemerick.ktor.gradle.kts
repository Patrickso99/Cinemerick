plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(lib("ktor-core"))
        implementation(lib("ktor-content-negotiation"))
        implementation(lib("ktor-serialization-json"))
    }
}

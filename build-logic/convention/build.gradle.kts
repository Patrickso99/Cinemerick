plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.build.kotlin.gradle.plugin)
    implementation(libs.build.kotlin.serialization)
    implementation(libs.build.compose.compiler)
    implementation(libs.build.compose.gradle.plugin)
    implementation(libs.build.android.gradle.plugin)
}

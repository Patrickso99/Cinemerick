import com.preichert.cinemerick.convention.AppVersion

plugins {
    id("com.preichert.convention.android.application")
}

dependencies {
    implementation(project(":app"))
    implementation(libs.koin.android)
    implementation(libs.androidx.activity.compose)
}

base {
    archivesName.set("cinemerick-v${AppVersion.NAME}-${AppVersion.CODE}")
}

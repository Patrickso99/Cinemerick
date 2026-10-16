import com.preichert.cinemerick.convention.AppInfo

plugins {
    id("com.preichert.convention.android.application")
}

dependencies {
    implementation(project(":app"))
    implementation(libs.koin.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
}

base {
    archivesName.set("${AppInfo.NAME.lowercase()}-v${AppInfo.VERSION_NAME}-${AppInfo.VERSION_CODE}")
}

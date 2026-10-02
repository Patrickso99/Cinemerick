import com.preichert.cinemerick.convention.AppVersion

plugins {
    id("com.preichert.convention.android.application")
}

dependencies {
    implementation(project(":app"))
    implementation(libs.koin.android)
    implementation(libs.androidx.activity.compose)
}

val renameApks = tasks.register("renameApks") {
    val apkDir = layout.buildDirectory.dir("outputs/apk")
    val versionName = AppVersion.NAME
    val versionCode = AppVersion.CODE
    doLast {
        apkDir.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension == "apk" }
            .forEach { file ->
                val variantSuffix = if (file.parentFile?.name?.contains("debug") == true) "-debug" else ""
                val newName = "cinemerick-v$versionName-$versionCode$variantSuffix.apk"
                if (file.name != newName && file.renameTo(File(file.parentFile, newName))) {
                    println("Renamed: ${file.name} -> $newName")
                }
            }
    }
}

tasks.matching { it.name.startsWith("assemble") }.configureEach {
    finalizedBy(renameApks)
}

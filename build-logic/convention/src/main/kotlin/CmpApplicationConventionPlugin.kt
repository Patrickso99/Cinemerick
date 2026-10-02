import com.preichert.cinemerick.convention.AppVersion
import com.preichert.cinemerick.convention.BASE_PACKAGE
import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.configureAndroidTarget
import com.preichert.cinemerick.convention.configureDesktopTarget
import com.preichert.cinemerick.convention.composeDesktopCurrentOs
import com.preichert.cinemerick.convention.configureIosTargets
import com.preichert.cinemerick.convention.libs
import com.preichert.cinemerick.convention.pathToPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.DesktopExtension

class CmpApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.kotlin.multiplatform.library)
                apply(libs.plugins.kotlin.multiplatform)
                apply(libs.plugins.compose.multiplatform)
                apply(libs.plugins.kotlin.compose)
            }

            configureAndroidTarget(pathToPackageName())
            configureDesktopTarget()
            configureIosTargets(frameworkName = "ComposeApp")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("desktopMain").dependencies {
                    implementation(composeDesktopCurrentOs())
                }
            }

            extensions.configure<ComposeExtension> {
                (this as ExtensionAware).extensions.configure<DesktopExtension>("desktop") {
                    application {
                        mainClass = "$BASE_PACKAGE.MainKt"
                        nativeDistributions {
                            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
                            packageName = "Cinemerick"
                            packageVersion = AppVersion.NAME
                            val iconsDir = file("src/desktopMain/resources")
                            macOS { iconFile.set(iconsDir.resolve("icon.icns")) }
                            windows { iconFile.set(iconsDir.resolve("icon.ico")) }
                            linux { iconFile.set(iconsDir.resolve("icon.png")) }
                        }
                    }
                }
            }
        }
    }
}

import com.preichert.cinemerick.convention.BASE_PACKAGE
import com.preichert.cinemerick.convention.configureAndroidTarget
import com.preichert.cinemerick.convention.configureDesktopTarget
import com.preichert.cinemerick.convention.configureIosTargets
import com.preichert.cinemerick.convention.configureWasmTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.DesktopExtension

class CmpApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.preichert.convention.android.application")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            configureAndroidTarget()
            configureDesktopTarget()
            configureIosTargets(frameworkName = "ComposeApp")
            configureWasmTarget()

            dependencies {
                "desktopMainImplementation"(ComposePlugin.DesktopDependencies.currentOs)
            }

            extensions.configure<ComposeExtension> {
                (this as ExtensionAware).extensions.configure<DesktopExtension>("desktop") {
                    application {
                        mainClass = "$BASE_PACKAGE.MainKt"
                        nativeDistributions {
                            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                            packageName = "Cinemerick"
                            packageVersion = "1.0.0"
                        }
                    }
                }
            }
        }
    }
}

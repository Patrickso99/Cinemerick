import com.preichert.cinemerick.convention.ConventionPlugin
import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.libs
import com.preichert.cinemerick.convention.pathToPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(ConventionPlugin.KMP_LIBRARY)
                apply(libs.plugins.kotlin.compose)
                apply(libs.plugins.compose.multiplatform)
            }

            extensions.configure<ComposeExtension> {
                (this as ExtensionAware).extensions.configure<ResourcesExtension>("resources") {
                    publicResClass = true
                    packageOfResClass = "${pathToPackageName()}.resources"
                }
            }

            extensions.configure<ComposeCompilerGradlePluginExtension> {
                stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.compose.runtime)
                    implementation(libs.compose.foundation)
                    implementation(libs.compose.material3)
                    implementation(libs.compose.ui)
                    implementation(libs.compose.resources)
                    implementation(libs.compose.ui.tooling.preview)
                }
                sourceSets.named("androidMain") {
                    dependencies {
                        implementation(libs.compose.ui.tooling)
                    }
                }
                sourceSets.named("desktopMain") {
                    dependencies {
                        implementation(libs.compose.ui.tooling)
                    }
                }
            }
        }
    }
}

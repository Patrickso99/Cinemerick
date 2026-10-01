import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.preichert.convention.kmp.library")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.compose")
            }

            dependencies {
                "commonMainImplementation"(libs.findLibrary("compose-runtime").get())
                "commonMainImplementation"(libs.findLibrary("compose-foundation").get())
                "commonMainImplementation"(libs.findLibrary("compose-material3").get())
                "commonMainImplementation"(libs.findLibrary("compose-ui").get())
                "commonMainImplementation"(libs.findLibrary("compose-resources").get())
                "commonMainImplementation"(libs.findLibrary("compose-ui-tooling-preview").get())
            }
        }
    }
}

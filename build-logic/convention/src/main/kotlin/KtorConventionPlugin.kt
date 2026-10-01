import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KtorConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
            }

            dependencies {
                "commonMainApi"(libs.findLibrary("ktor-core").get())
                "commonMainImplementation"(libs.findLibrary("ktor-content-negotiation").get())
                "commonMainImplementation"(libs.findLibrary("ktor-serialization-json").get())
            }
        }
    }
}

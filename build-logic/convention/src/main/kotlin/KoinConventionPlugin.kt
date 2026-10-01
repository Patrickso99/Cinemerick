import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
            }

            dependencies {
                "commonMainImplementation"(libs.findLibrary("koin-core").get())
            }
        }
    }
}

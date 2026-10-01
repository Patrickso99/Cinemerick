import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.preichert.convention.cmp.library")
                apply("com.preichert.convention.koin")
            }

            dependencies {
                "commonMainImplementation"(libs.findLibrary("koin-compose").get())
                "commonMainImplementation"(libs.findLibrary("koin-compose-viewmodel").get())
                "commonMainImplementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "commonMainImplementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "commonMainImplementation"(libs.findLibrary("coroutines-core").get())
                "commonMainImplementation"(libs.findLibrary("datetime").get())
            }
        }
    }
}

import com.preichert.cinemerick.convention.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.multiplatform")
            }

            configureKotlinMultiplatform()
        }
    }
}

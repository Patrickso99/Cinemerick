import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.configureKotlinMultiplatform
import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.kotlin.multiplatform.library)
                apply(libs.plugins.kotlin.multiplatform)
            }

            configureKotlinMultiplatform()
        }
    }
}

import com.preichert.cinemerick.convention.ConventionPlugin
import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(ConventionPlugin.CMP_LIBRARY)
                apply(ConventionPlugin.KOIN)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.koin.compose)
                    implementation(libs.koin.compose.viewmodel)
                    implementation(libs.androidx.lifecycle.viewmodel.compose)
                    implementation(libs.androidx.lifecycle.runtime.compose)
                    implementation(libs.coroutines.core)
                    implementation(libs.datetime)
                }
            }
        }
    }
}

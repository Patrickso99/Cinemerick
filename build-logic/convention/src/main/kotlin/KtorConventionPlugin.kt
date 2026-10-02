import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KtorConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.plugins.kotlin.multiplatform)

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    api(libs.ktor.core)
                    implementation(libs.ktor.content.negotiation)
                    implementation(libs.ktor.serialization.json)
                }
            }
        }
    }
}

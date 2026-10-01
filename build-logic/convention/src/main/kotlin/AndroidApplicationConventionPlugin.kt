import com.android.build.api.dsl.ApplicationExtension
import com.preichert.cinemerick.convention.BASE_PACKAGE
import com.preichert.cinemerick.convention.configureKotlinAndroid
import com.preichert.cinemerick.convention.versionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                namespace = BASE_PACKAGE

                defaultConfig {
                    applicationId = BASE_PACKAGE
                    targetSdk = versionInt("androidTargetSdk")
                    versionCode = 1
                    versionName = "1.0"
                }

                configureKotlinAndroid(this)
            }
        }
    }
}

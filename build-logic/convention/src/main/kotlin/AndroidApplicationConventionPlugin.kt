import com.android.build.api.dsl.ApplicationExtension
import com.preichert.cinemerick.convention.AppVersion
import com.preichert.cinemerick.convention.BASE_PACKAGE
import com.preichert.cinemerick.convention.configureKotlinAndroid
import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.application)
                apply(libs.plugins.kotlin.compose)
            }

            extensions.configure<ApplicationExtension> {
                namespace = BASE_PACKAGE

                defaultConfig {
                    applicationId = BASE_PACKAGE
                    targetSdk = libs.versions.androidTargetSdk.get().toInt()
                    versionCode = AppVersion.CODE
                    versionName = AppVersion.NAME
                }

                configureKotlinAndroid(this)
            }
        }
    }
}

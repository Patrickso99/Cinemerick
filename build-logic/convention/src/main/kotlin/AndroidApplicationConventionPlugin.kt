import com.android.build.api.dsl.ApplicationExtension
import com.preichert.cinemerick.convention.AppInfo
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
                    versionCode = AppInfo.VERSION_CODE
                    versionName = AppInfo.VERSION_NAME
                    manifestPlaceholders["appName"] = AppInfo.NAME
                }

                configureKotlinAndroid(this)
            }
        }
    }
}

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.preichert.cinemerick.convention.AppInfo
import com.preichert.cinemerick.convention.BASE_PACKAGE
import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.StringReader
import java.util.Properties

class BuildKonfigConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.plugins.buildkonfig)

            val gitHash = providers.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                isIgnoreExitValue = true
            }.standardOutput.asText.map { it.trim().ifEmpty { "unknown" } }.getOrElse("unknown")

            // TMDB read access token: `tmdb.token` in the git-ignored local.properties, else the TMDB_TOKEN environment
            // variable (CI), else empty. An empty token turns the TMDB lookup off.
            val tmdbToken = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText
                .map { Properties().apply { load(StringReader(it)) }.getProperty("tmdb.token")?.trim().orEmpty().ifEmpty { null } }
                .orElse(providers.environmentVariable("TMDB_TOKEN").map { it.trim() })
                .getOrElse("")

            extensions.configure<BuildKonfigExtension> {
                packageName.set("$BASE_PACKAGE.core.domain")
                // Public object: read by feature modules.
                exposeObjectWithName.set("BuildKonfig")
                defaultConfigs {
                    buildConfigField(Type.STRING, "APP_NAME", AppInfo.NAME)
                    buildConfigField(Type.STRING, "VERSION_NAME", AppInfo.VERSION_NAME)
                    buildConfigField(Type.INT, "VERSION_CODE", AppInfo.VERSION_CODE.toString())
                    buildConfigField(Type.STRING, "GIT_HASH", gitHash)
                    buildConfigField(Type.STRING, "TMDB_TOKEN", tmdbToken)
                }
            }
        }
    }
}

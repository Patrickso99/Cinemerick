import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.preichert.cinemerick.convention.BASE_PACKAGE
import com.preichert.cinemerick.convention.apply
import com.preichert.cinemerick.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.StringReader
import java.util.Properties

/**
 * Generates `TmdbConfig`, the TMDB read token, in the data layer only: the domain module and the UI never see it.
 */
class TmdbBuildKonfigConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.plugins.buildkonfig)

            // TMDB read access token: `tmdb.token` in the git-ignored local.properties, else the TMDB_TOKEN environment
            // variable (CI), else empty. An empty token turns the TMDB lookup off.
            val tmdbToken = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText
                .map { Properties().apply { load(StringReader(it)) }.getProperty("tmdb.token")?.trim().orEmpty().ifEmpty { null } }
                .orElse(providers.environmentVariable("TMDB_TOKEN").map { it.trim() })
                .getOrElse("")

            extensions.configure<BuildKonfigExtension> {
                packageName.set("$BASE_PACKAGE.core.data")
                // Public object: read by the feature data modules.
                exposeObjectWithName.set("TmdbConfig")
                defaultConfigs {
                    buildConfigField(Type.STRING, "TMDB_TOKEN", tmdbToken)
                }
            }
        }
    }
}

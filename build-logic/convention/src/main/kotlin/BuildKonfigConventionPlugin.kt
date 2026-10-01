import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.preichert.cinemerick.convention.AppVersion
import com.preichert.cinemerick.convention.BASE_PACKAGE
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class BuildKonfigConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.codingfeline.buildkonfig")

            val gitHash = providers.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                isIgnoreExitValue = true
            }.standardOutput.asText.map { it.trim().ifEmpty { "unknown" } }.getOrElse("unknown")

            extensions.configure<BuildKonfigExtension> {
                packageName.set("$BASE_PACKAGE.core.domain")
                // Public object: read by feature modules.
                exposeObjectWithName.set("BuildKonfig")
                defaultConfigs {
                    buildConfigField(Type.STRING, "VERSION_NAME", AppVersion.NAME)
                    buildConfigField(Type.INT, "VERSION_CODE", AppVersion.CODE.toString())
                    buildConfigField(Type.STRING, "GIT_HASH", gitHash)
                }
            }
        }
    }
}

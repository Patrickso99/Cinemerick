import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> = libs.findLibrary(alias).get()

fun Project.versionInt(alias: String): Int = libs.findVersion(alias).get().requiredVersion.toInt()

fun Project.androidNamespace(): String =
    "it.cinemerick." + path.removePrefix(":").replace(":", ".").replace("-", "")

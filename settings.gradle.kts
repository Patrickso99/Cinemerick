pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Cinemerick"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")
include(":androidApp")
include(":core:domain")
include(":core:data")
include(":core:testing")
include(":core:presentation")
include(":core:designsystem")
include(":feature:showtimes:domain")
include(":feature:showtimes:data")
include(":feature:showtimes:presentation")
include(":feature:settings:presentation")

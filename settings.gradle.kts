pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DC Android Foundation"
include(":foundation")
include(":app")
include(":ui:designsystem")
include(":runtime:concurrency")
include(":data:preferences")
include(":observability")
include(":data:network")
include(":data:database")
include(":runtime:scheduling")
include(":platform:navigation")
include(":platform:permission")
include(":security")
include(":testing")

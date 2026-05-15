pluginManagement {
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Gradle 9: libs.versions.toml otomatik algılanır, from() gerekmez
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Dersium"
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:ui")
include(":core:network")
include(":feature:home")
include(":feature:students")
include(":feature:lessons")
include(":feature:calendar")
include(":feature:financial")
include(":feature:reports")
include(":feature:auth")
include(":feature:settings")

include(":feature:export")

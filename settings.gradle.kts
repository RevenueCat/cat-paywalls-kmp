rootProject.name = "cat-paywalls-kmp"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    mavenLocal()
    mavenCentral()
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
  }
}

// Core modules
include(":core:model")
include(":core:network")
include(":core:data")
include(":core:designsystem")
include(":core:navigation")

// Feature modules
include(":feature:home")
include(":feature:article")
include(":feature:paywalls")
include(":feature:account")
include(":feature:subscriptions")

// App modules
include(":composeApp")

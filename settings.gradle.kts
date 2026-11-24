pluginManagement {
    repositories {
        google()
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
}
rootProject.name = "kotlin-android-tutorial"
include(":app")
include(":core:network")
include(":core:storage")
include(":core:database")
include(":core:ui")
include(":feature:auth")
include(":feature:product-catalog")

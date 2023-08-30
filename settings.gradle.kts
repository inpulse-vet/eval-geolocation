pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
    }
}

rootProject.name = "eval-geolocation"

include(":model")
include(":client")
include(":ktor-client")
include(":server")
include(":ktor-server")
include(":android")


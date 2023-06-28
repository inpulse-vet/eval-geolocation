rootProject.name = "eval-geolocation"

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
    }
}

include(":model")
include(":client")
include(":ktor-client")

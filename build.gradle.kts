plugins {
    val kotlinVersion = "1.9.0"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false

    val androidAgpVersion = "8.1.1"
    id("com.android.application") version androidAgpVersion apply false
    id("com.android.library") version androidAgpVersion apply false
}

allprojects {
    group = "vet.inpulse"
    version = "1.0-SNAPSHOT"
}
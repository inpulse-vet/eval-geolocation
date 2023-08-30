plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    java
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api(project(":model"))
    testImplementation(kotlin("test"))
}


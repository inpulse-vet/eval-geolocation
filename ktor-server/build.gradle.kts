plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("com.google.cloud.tools.jib") version "3.3.2"
    java
}

group = "vet.inpulse.geolocation"
version = "0.0.1"

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":server"))

    val ktorVersion = "2.3.1"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    val exposedVersion = "0.37.3"

    // database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("net.postgis:postgis-jdbc:2.2.0")

    implementation("com.zaxxer:HikariCP:5.0.1")

    // testing
    testImplementation("org.testcontainers:postgresql:1.17.6")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    testImplementation(kotlin("test"))
}

jib {
    to {
        // set image, tag and registry

        auth {
            username = project.findProperty("nexus_username") as String
            password = project.findProperty("nexus_password") as String
        }
    }
}

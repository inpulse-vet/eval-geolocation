plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("com.google.cloud.tools.jib") version "3.3.2"
    id("me.champeau.jmh") version "0.6.8"
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
    implementation("io.ktor:ktor-client-apache:2.3.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.1")
    implementation("io.ktor:ktor-serialization-jackson:2.3.1")

    val ktorVersion = "2.3.1"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    val koinVersion = "3.4.0"

    // benchmark
    jmh("org.openjdk.jmh:jmh-core:1.36")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.36")

    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.36")

    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.36")

    // Koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")

    // CSV parsing
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.1")

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

jmh {
    version = "1.36"
}

jib {
    from {
        image = "openjdk:17-alpine"
    }

    to {
        image = "vet-inpulse/eval-geolocation"
        tags = setOf("latest")
    }

    container {
        mainClass = "vet.inpulse.geolocation.server.MainKt"
    }
}
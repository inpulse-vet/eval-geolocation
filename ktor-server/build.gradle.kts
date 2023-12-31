plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version "3.3.2"
    id("me.champeau.jmh") version "0.6.8"
    java
}

group = "vet.inpulse"
version = "0.0.3"

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":server"))

    val ktorVersion = "2.3.3"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.4.8")
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

jib {
    from {
        image = "openjdk:17-alpine"
    }

    to {
        image = "registry.incloud.vet/${project.group}/geolocation:${project.version}"
    }

    container {
        mainClass = "vet.inpulse.geolocation.server.MainKt"
    }
}
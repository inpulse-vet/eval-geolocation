package vet.inpulse.geolocation.server.test

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.*
import java.util.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
open class Benchmark {

    private lateinit var engine: TestApplicationEngine

    @Setup(Level.Trial)
    fun setup() {
        engine = TestApplicationEngine(createTestEnvironment())
        engine.start(wait = false)

        engine.application.apply {
            configureKoin()
            configureAuthentication()
            configureSerialization()
        }
    }

    @TearDown(Level.Trial)
    fun teardown() {
        engine.stop(0,0, TimeUnit.SECONDS)
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun locationBenchmark(blackhole: Blackhole) {
        val call = engine.handleRequest(HttpMethod.Get, "/restaurants?lat=34&long=34&n=10&distance=1000")
        blackhole.consume(call)
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun insertBenchmark(blackhole: Blackhole) {
        val restaurantDetails = RestaurantDetails(
            UUID.randomUUID(), "Benchmark Restaurant",
            Location(Latitude(34F), Longitude(34F)),
            "Test Street Address", "Test Phone", "Test Website",
            OpenHours(LocalTime(1, 0), LocalTime(5, 0))
        )

        val call = engine.handleRequest(HttpMethod.Post, "/restaurants") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(
                HttpHeaders.Authorization,
                "Basic ${Base64.getEncoder().encodeToString("user:password".toByteArray())}"
            )
            setBody(Json.encodeToJsonElement(restaurantDetails).toString())
        }

        blackhole.consume(call)
    }
}
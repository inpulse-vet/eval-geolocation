package vet.inpulse.geolocation.server.test

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.configureAuthentication
import vet.inpulse.geolocation.server.configureRouting
import vet.inpulse.geolocation.server.configureSerialization
import vet.inpulse.geolocation.server.database.DatabaseFactory
import vet.inpulse.geolocation.server.repository.RestaurantServiceImpl
import vet.inpulse.geolocation.server.service.RestaurantRepositoryImpl
import vet.inpulse.server.RestaurantService
import java.util.Base64
import java.util.UUID
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class KtorServerTest {

    companion object {
        val postgresSQLContainer = PostgreSQLContainer(DockerImageName.parse("postgres:15.3"))
            .apply {
                withDatabaseName("testDb")
                withUsername("testUser")
                withPassword("testPassword")
            }

        val randomId: UUID = UUID.randomUUID()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            if (!postgresSQLContainer.isRunning) postgresSQLContainer.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgresSQLContainer.stop()
        }
    }

    private var service: RestaurantService? = null
    private lateinit var engine: TestApplicationEngine

    @BeforeEach
    fun setUp() {
        postgresSQLContainer.apply {
            DatabaseFactory.init(jdbcUrl, username, password)
        }
        this.service = RestaurantServiceImpl(RestaurantRepositoryImpl())

        engine = TestApplicationEngine()
        engine.start(wait = false)

        engine.application.configureSerialization()
        engine.application.configureAuthentication()
        engine.application.configureRouting(service!!)
    }

    @AfterEach
    fun tearDown() {
        engine.stop(1000, 1000)
    }

    @Test
    fun testAddRestaurant() {
        val restaurantDetails = RestaurantDetails(
            randomId, "Test Restaurant",
            Location(Latitude(70F), Longitude(80F)),
            "Test Street Address", "Test Phone", "Test Website",
            OpenHours(LocalTime(1, 0), LocalTime(5, 0))
        )

        val call = engine.handleRequest(HttpMethod.Post, "/restaurants") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(HttpHeaders.Authorization,
                "Basic ${Base64.getEncoder().encodeToString("user:password".toByteArray())}")
            setBody(Json.encodeToJsonElement(restaurantDetails).toString())
        }

        assertNotNull(call.response.status())
        assert(call.response.status()!!.isSuccess())
    }

    @Test
    fun getRestaurantDetails() {
        val call = engine.handleRequest(HttpMethod.Get, "/restaurants/$randomId")

        assertNotNull(call.response.status())
        assert(call.response.status()!!.isSuccess())
    }
}

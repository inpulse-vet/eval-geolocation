package vet.inpulse.geolocation.server.test

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
import org.slf4j.LoggerFactory

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.*
import vet.inpulse.geolocation.server.database.Configuration
import vet.inpulse.geolocation.server.database.DatabaseFactory
import java.util.Base64
import java.util.UUID
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class KtorServerTest {

    companion object {
        private val logger = LoggerFactory.getLogger(KtorServerTest::class.java)

        val postgresSQLContainer = PostgreSQLContainer<Nothing>(
            DockerImageName.parse("postgis/postgis:13-3.1")
                .asCompatibleSubstituteFor("postgres")
        )
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

    private lateinit var engine: TestApplicationEngine

    @BeforeEach
    fun setUp() {
        postgresSQLContainer.apply {
            DatabaseFactory().createDatabaseConnection(Configuration(jdbcUrl, username, password))
        }

        engine = TestApplicationEngine()
        engine.start(wait = false)

        engine.application.apply {
            configureKoin()
            configureDatabase()
            configureSerialization()
            configureAuthentication()
            configureRouting()
        }

    }

    @AfterEach
    fun tearDown() {
        engine.stop(1000, 1000)
    }

    @Test
    fun insertRestaurant() {
        val restaurantDetails = RestaurantDetails(
            randomId, "Test Restaurant",
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

        val status = call.response.status()
        assertNotNull(status)
        assert(status.isSuccess())
    }

    @Test
    fun getRestaurantDetails() {
        val call = engine.handleRequest(HttpMethod.Get, "/restaurants/$randomId")
        val status = call.response.status()

        assertNotNull(status)
        assert(status.isSuccess())

        logger.info(call.response.content)
    }

    @Test
    fun getNearbyRestaurants() {
        val call = engine.handleRequest(HttpMethod.Get, "/restaurants?lat=34&long=34&n=10&distance=100")
        val status = call.response.status()

        assertNotNull(status)
        assert(status.isSuccess())

        logger.info(call.response.content)
    }
}

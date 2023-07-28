package vet.inpulse.geolocation.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.exposedLogger
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.data.PrincipalAuthentication
import vet.inpulse.geolocation.server.database.Configuration
import vet.inpulse.geolocation.server.database.DatabaseFactory
import vet.inpulse.geolocation.server.monitor.configureServerMonitoring
import vet.inpulse.geolocation.server.monitor.getReadyRouting
import vet.inpulse.geolocation.server.processor.CSVDatabaseProcessor
import vet.inpulse.geolocation.server.repository.RestaurantRepositoryImpl
import vet.inpulse.geolocation.server.service.RestaurantServiceImpl
import vet.inpulse.server.RestaurantRepository
import vet.inpulse.server.RestaurantService

const val CSV_FILE = "restaurants.csv"

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureDatabase()
    configureStatusPages()

    configureAuthentication()
    configureServerMonitoring()
    configureSerialization()
    configureRouting()
}

fun Application.configureDatabase() {
    val databaseFactory by inject<DatabaseFactory>()
    databaseFactory.createDatabaseConnection(
        Configuration(
            System.getenv("POSTGRES_URL"),
            System.getenv("POSTGRES_USER"),
            System.getenv("POSTGRES_PASSWORD")
        )
    )

    val restaurantService by inject<RestaurantService>()

    val resource = ClassLoader.getSystemResourceAsStream(CSV_FILE)
    if (resource == null) {
        exposedLogger.error("Could not load CSV file")
        return
    }

    launch(Dispatchers.IO) {
        restaurantService.loadDataFromCSV(resource)
    }
}

fun Application.configureStatusPages() = install(StatusPages) {
    exception<ApplicationException> { call, cause ->
        val statusCode = when (cause.error) {
            Error.INVALID_ID -> HttpStatusCode.BadRequest
            Error.MALFORMED_INPUT -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.NotFound
        }
        call.respond(statusCode)
    }
}

fun Application.configureKoin() = install(Koin) {
    val appModule = module {
        single<DatabaseFactory> { DatabaseFactory() }
        single<CSVDatabaseProcessor> { CSVDatabaseProcessor() }
        single<RestaurantRepository> { RestaurantRepositoryImpl() }
        single<RestaurantService> { RestaurantServiceImpl(get(), get()) }
    }
    modules(appModule)
}

fun Application.configureSerialization() = install(ContentNegotiation) {
    json()
}

fun Application.configureAuthentication() = install(Authentication) {
    basic("auth-basic") {
        validate { credentials ->
            if (credentials.name == "user" && credentials.password == "password") {
                PrincipalAuthentication(credentials.name)
            } else null
        }
    }
}

fun Application.configureRouting() {
    val restaurantService by inject<RestaurantService>()

    routing {
        getReadyRouting()

        get("/health") {
            val statusCode = if (restaurantService.checkDatabaseStatus()!!)
                HttpStatusCode.OK
            else HttpStatusCode.ServiceUnavailable

            call.respond(statusCode)
        }

        get("/restaurants") {
            val parameters = call.parameters

            val latitude = parameters["lat"]
            val longitude = parameters["long"]
            val batch = parameters["n"]

            if (latitude.isNullOrEmpty() or longitude.isNullOrEmpty() or batch.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Any of the inputs is malformed")
                return@get
            }

            val location = Location(Latitude(latitude!!.toFloat()), Longitude(longitude!!.toFloat()))

            val maximumDistance = parameters["distance"]?.toDoubleOrNull()
            val message = restaurantService.getNearbyRestaurants(location, batch!!.toInt(), maximumDistance)

            call.respondText(Json.encodeToString(message), ContentType.Application.Json, HttpStatusCode.OK)
        }

        get("/restaurants/{restaurantId}") {
            val restaurantId = call.parameters["restaurantId"]

            if (restaurantId.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val restaurantDetails = restaurantService.getRestaurantDetails(restaurantId)
            if (restaurantDetails == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(HttpStatusCode.OK, restaurantDetails)
        }
        authenticate("auth-basic") {
            post("/restaurants") {
                val principal = call.principal<PrincipalAuthentication>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val details = call.receive<RestaurantDetails>()
                restaurantService.addRestaurant(details)

                call.respond(HttpStatusCode.Created, details)
            }
        }
    }
}

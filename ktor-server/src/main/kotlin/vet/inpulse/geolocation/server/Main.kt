package vet.inpulse.geolocation.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.exposedLogger
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.data.PrincipalAuthentication
import vet.inpulse.geolocation.server.database.Configuration
import vet.inpulse.geolocation.server.database.DatabaseFactory
import vet.inpulse.geolocation.server.health.DatabaseHealthRepositoryImpl
import vet.inpulse.geolocation.server.health.HealthControllerImpl
import vet.inpulse.geolocation.server.health.MonitoringPlugin
import vet.inpulse.geolocation.server.processor.CSVDatabaseProcessor
import vet.inpulse.geolocation.server.repository.RestaurantRepositoryImpl
import vet.inpulse.geolocation.server.service.RestaurantServiceImpl
import vet.inpulse.server.RestaurantRepository
import vet.inpulse.server.RestaurantService
import vet.inpulse.server.health.DatabaseHealthRepository
import vet.inpulse.server.health.HealthController

const val CSV_FILE = "restaurants.csv"

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureMonitoringPlugin()
    configureDatabase()
    configureStatusPages()

    configureAuthentication()
    configureSerialization()
    configureRouting()
}

fun Application.configureMonitoringPlugin() = install(MonitoringPlugin)

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

        single<DatabaseHealthRepository> { DatabaseHealthRepositoryImpl() }
        single<HealthController> { HealthControllerImpl(get()) }
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

package vet.inpulse.geolocation.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import vet.inpulse.geolocation.ApplicationException
import vet.inpulse.geolocation.Error
import vet.inpulse.geolocation.server.data.PrincipalAuthentication
import vet.inpulse.geolocation.server.database.DatabaseFactory
import vet.inpulse.geolocation.server.repository.RestaurantRepositoryImpl
import vet.inpulse.geolocation.server.routing.configureRouting
import vet.inpulse.geolocation.server.service.RestaurantServiceImpl
import vet.inpulse.server.RestaurantRepository
import vet.inpulse.server.RestaurantService

fun main() {
    embeddedServer(Netty, port = 8081, host = "localhost") {
        DatabaseFactory.init()

        val service: RestaurantService = RestaurantServiceImpl()
        val repository: RestaurantRepository = RestaurantRepositoryImpl(service)

        install(StatusPages) {
            exception<ApplicationException> { call, cause ->
                val statusCode = when (cause.error) {
                    Error.INVALID_ID -> HttpStatusCode.BadRequest
                    Error.MALFORMED_INPUT -> HttpStatusCode.BadRequest
                    else -> HttpStatusCode.NotFound
                }
                call.respond(statusCode)
            }
        }

        install(Authentication) {
            basic("auth-basic") {
                validate { credentials ->
                    if (credentials.name == "user" && credentials.password == "password") {
                        PrincipalAuthentication(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }

        install(ContentNegotiation) {
            json()
        }

        configureRouting(repository)

    }.start(wait = true)
}

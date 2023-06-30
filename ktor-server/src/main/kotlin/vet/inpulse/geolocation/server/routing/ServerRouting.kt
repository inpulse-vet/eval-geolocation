package vet.inpulse.geolocation.server.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.data.PrincipalAuthentication
import vet.inpulse.server.RestaurantRepository

fun Application.configureRouting(restaurantRepository: RestaurantRepository) {
    routing {
        get("/restaurants") {
            val parameters = call.parameters

            val latitude = parameters["lat"]
            val longitude = parameters["long"]
            val batch = parameters["n"]

            if (latitude.isNullOrEmpty() or longitude.isNullOrEmpty() or batch.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Any of the inputs is malformed")
                return@get
            }

            val location = Location(
                Latitude(latitude!!.toFloat()),
                Longitude(longitude!!.toFloat())
            )

            val maximumDistance = parameters["distance"]?.toDoubleOrNull()
            val message = restaurantRepository.getNearbyRestaurants(location, batch!!.toInt(), maximumDistance)

            call.respondText(Json.encodeToString(message), ContentType.Application.Json, HttpStatusCode.OK)
        }

        get("/restaurants/{restaurantId}") {
            val restaurantId = call.parameters["restaurantId"]

            if (restaurantId.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val restaurantDetails = restaurantRepository.getRestaurantDetails(restaurantId)
            if (restaurantDetails == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(HttpStatusCode.OK, restaurantDetails)
        }
        authenticate("auth-basic") {
            post("/restaurants") {
                val principal = call.principal<PrincipalAuthentication>()
                application.log.info(principal.toString())

                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val details = call.receive<RestaurantDetails>()
                restaurantRepository.addRestaurant(details)

                call.respond(HttpStatusCode.Created, details)
            }
        }
    }
}
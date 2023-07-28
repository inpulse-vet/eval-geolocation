package vet.inpulse.geolocation.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import vet.inpulse.geolocation.Latitude
import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Longitude
import vet.inpulse.geolocation.RestaurantDetails
import vet.inpulse.geolocation.server.data.PrincipalAuthentication
import vet.inpulse.server.RestaurantService

fun Application.configureRouting() {
    val restaurantService by inject<RestaurantService>()

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
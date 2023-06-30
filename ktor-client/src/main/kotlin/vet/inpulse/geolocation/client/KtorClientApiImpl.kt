package vet.inpulse.geolocation.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import vet.inpulse.geolocation.ApplicationException
import vet.inpulse.geolocation.Error
import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Restaurant
import vet.inpulse.geolocation.RestaurantDetails
import java.util.*

class KtorClientApiImpl(
    private val baseUrl: Url,
    user: String,
    password: String,
    client: HttpClient = HttpClient(CIO),
    timeoutMillis: Long = 1000,
    json: Json = DefaultJson,
    log: Boolean = true,
) : ClientApi {

    private val client = client.config {
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(user, password)
                }
            }
        }
        if (log) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
    }

    override suspend fun listRestaurants(
        location: Location, numberOfResults: Int, rangeKm: Float?
    ): Result<List<Restaurant>> {
        return suspendResultOf {
            val requestUrl = URLBuilder(baseUrl).apply {
                appendPathSegments("restaurants")
                parameters.apply {
                    append("lat", location.latitude.value.toString())
                    append("long", location.longitude.value.toString())
                    append("n", numberOfResults.toString())
                    if (rangeKm != null) {
                        append("distance", rangeKm.toString())
                    }
                }
            }.build()
            val response = client.get(requestUrl)
            when (response.status) {
                HttpStatusCode.OK -> response.body<List<Restaurant>>()
                HttpStatusCode.BadRequest -> throw ApplicationException(Error.MALFORMED_INPUT)
                else -> throw ApplicationException(Error.UNKNOWN)
            }
        }
    }

    override suspend fun getRestaurantDetails(restaurantId: UUID): Result<RestaurantDetails> {
        val url = baseUrl / "restaurants" / restaurantId.toString()
        return suspendResultOf {
            val response = client.get(url)
            when (response.status) {
                HttpStatusCode.OK -> response.body<RestaurantDetails>()
                HttpStatusCode.BadRequest -> throw ApplicationException(Error.MALFORMED_INPUT)
                HttpStatusCode.NotFound -> throw ApplicationException(Error.INVALID_ID)
                else -> throw ApplicationException(Error.UNKNOWN)
            }
        }
    }

    override suspend fun insertRestaurant(restaurantDetails: RestaurantDetails): Result<RestaurantDetails> {
        val url = baseUrl / "restaurants"
        return suspendResultOf {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
/*
                basicAuth("username", "password")
*/
                setBody(restaurantDetails)
            }
            when (response.status) {
                HttpStatusCode.Created -> response.body<RestaurantDetails>()
                HttpStatusCode.BadRequest -> throw ApplicationException(Error.MALFORMED_DATA)
                HttpStatusCode.Unauthorized -> throw ApplicationException(Error.UNAUTHORIZED)
                else -> throw ApplicationException(Error.UNKNOWN)
            }
        }
    }

    private inline fun <T> suspendResultOf(block: () -> T): Result<T> {
        return runCatching {
            block()
        }
    }

    private operator fun Url.div(path: String): Url {
        return URLBuilder(this).apply {
            appendPathSegments(path)
        }.build()
    }

}
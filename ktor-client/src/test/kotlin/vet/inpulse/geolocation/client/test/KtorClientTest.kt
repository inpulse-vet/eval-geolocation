package vet.inpulse.geolocation.client.test

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import org.slf4j.LoggerFactory
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.client.KtorClientApiImpl
import java.util.*
import kotlin.test.Test

class KtorClientTest {
    private val client = KtorClientApiImpl(Url("http://localhost:8081"), "user", "password")

    private val logger = LoggerFactory.getLogger(KtorClientTest::class.java)

    @Test
    fun `test insert restaurant`(): Unit = runBlocking {
        val location = Location(Latitude(33.40f), Longitude(33.20f))
        val openHours = OpenHours(
            LocalTime(11, 3, 4),
            LocalTime(12, 3, 4)
        )

        val restaurantDetails = RestaurantDetails(
            UUID.randomUUID(), "Restaurante Novo", location, "Nova rua 20",
            "19 988605013", "www.pizzaria.com", openHours
        )

        val restaurants = client.insertRestaurant(restaurantDetails)
        assert(restaurants.isSuccess)
    }

    @Test
    fun `test get restaurant details`(): Unit = runBlocking {
        val restaurants = client.getRestaurantDetails(
            UUID.fromString("8a7efc66-9c0b-4e66-a53e-3b4f3c4a58d1")
        )
        assert(restaurants.isSuccess)
    }

    @Test
    fun `test list restaurants`(): Unit = runBlocking {
        val restaurants = client.listRestaurants(Location(Latitude(33f), Longitude(33f)), 10)
        logger.info(restaurants.toString())

        assert(restaurants.isSuccess)
        assert(restaurants.getOrThrow().isNotEmpty())
    }
}

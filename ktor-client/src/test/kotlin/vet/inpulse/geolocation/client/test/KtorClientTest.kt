package vet.inpulse.geolocation.client.test

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import vet.inpulse.geolocation.Latitude
import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Longitude
import vet.inpulse.geolocation.client.KtorClientApiImpl
import kotlin.test.Test

class KtorClientTest {
    val client = KtorClientApiImpl(Url("http://localhost:8081"), "user", "password")

    @Test
    fun `test list restaurants`(): Unit = runBlocking {
        val restaurants = client.listRestaurants(Location(Latitude(33f), Longitude(33f)), 10, 1f)
        assert(restaurants.isSuccess)
        assert(restaurants.getOrThrow().isNotEmpty())
    }
}

package vet.inpulse.geolocation.server.repository

import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.postgis.Point
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.database.RestaurantTable
import vet.inpulse.geolocation.server.database.distance
import vet.inpulse.geolocation.server.extensions.parseBatch
import vet.inpulse.geolocation.server.extensions.parseInsert
import vet.inpulse.geolocation.server.extensions.toRestaurant
import vet.inpulse.server.RestaurantRepository
import java.util.*

class RestaurantRepositoryImpl : RestaurantRepository {

    override suspend fun addNewRestaurants(restaurants: List<RestaurantDetails>) {
        newSuspendedTransaction {
            RestaurantTable.batchInsert(restaurants) {
                parseBatch(it)
            }
        }
    }

    override suspend fun addNewRestaurant(restaurantDetails: RestaurantDetails): Boolean {
        return runCatching {
            newSuspendedTransaction {
                RestaurantTable.insert {
                    it.parseInsert(restaurantDetails)
                }
            }
        }.isSuccess
    }

    override suspend fun getRestaurantDetails(restaurantId: UUID): RestaurantDetails? {
        return newSuspendedTransaction {
            RestaurantTable.select {
                RestaurantTable.id eq restaurantId
            }.singleOrNull()?.let { result ->
                val rawLocation = result[RestaurantTable.location]
                val location = Location(Latitude(rawLocation.x.toFloat()), Longitude(rawLocation.y.toFloat()))

                val closingTime = LocalTime.parse(result[RestaurantTable.closingTime])
                val openingTime = LocalTime.parse(result[RestaurantTable.openingTime])

                RestaurantDetails(
                    restaurantId, result[RestaurantTable.name], location,
                    result[RestaurantTable.streetAddress], result[RestaurantTable.phone],
                    result[RestaurantTable.website], OpenHours(openingTime, closingTime)
                )
            }
        }
    }

    override suspend fun getNearbyRestaurants(
        location: Location,
        batch: Int,
        maximumDistance: Double?
    ): List<Restaurant> {
        val point = Point(location.longitude.value.toDouble(), location.latitude.value.toDouble())

        return newSuspendedTransaction {
            RestaurantTable.slice(RestaurantTable.id, RestaurantTable.name, RestaurantTable.location)
                .select(RestaurantTable.location.distance(point, maximumDistance ?: 1000.0))
                .limit(batch)
                .mapNotNull { it.toRestaurant() }
        }
    }
}

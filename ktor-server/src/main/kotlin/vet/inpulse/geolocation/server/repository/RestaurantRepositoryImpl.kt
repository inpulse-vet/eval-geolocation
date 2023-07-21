package vet.inpulse.geolocation.server.repository

import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.postgis.Point
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.database.DistanceOp
import vet.inpulse.geolocation.server.database.RestaurantTable
import vet.inpulse.server.RestaurantRepository
import java.util.*

class RestaurantRepositoryImpl : RestaurantRepository {

    override suspend fun addNewRestaurant(restaurantDetails: RestaurantDetails): Boolean {
        val point = Point(
              restaurantDetails.location.latitude.value.toDouble(),
              restaurantDetails.location.longitude.value.toDouble()
        )

        newSuspendedTransaction {
            RestaurantTable.insert { it ->
                it[id] = restaurantDetails.id
                it[name] = restaurantDetails.name
                it[location] = point
                it[streetAddress] = restaurantDetails.streetAddress
                it[phone] = restaurantDetails.phone
                it[website] = restaurantDetails.website

                restaurantDetails.openHours?.let { openHours ->
                    it[openingTime] = openHours.openingTime.toString()
                    it[closingTime] = openHours.closingTime.toString()
                }
            }
        }

        return true
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


    override suspend fun getNearbyRestaurants(location: Location, batch: Int, maximumDistance: Double?): List<Restaurant> {
        val point = Point(location.longitude.value.toDouble(), location.latitude.value.toDouble())

        return newSuspendedTransaction {
            RestaurantTable.selectAll()
                  .limit(batch)
                  .andWhere { DistanceOp(RestaurantTable.location, point, maximumDistance ?: 1000.0) }
                  .mapNotNull { it.toRestaurant() }
                  .toList()
        }
    }

    private fun ResultRow.toRestaurant(): Restaurant {
        val location = this[RestaurantTable.location]
        val latitude = Latitude(location.x.toFloat())
        val longitude = Longitude(location.y.toFloat())

        return Restaurant(
              id = this[RestaurantTable.id],
              name = this[RestaurantTable.name],
              location = Location(latitude, longitude)
        )
    }
}

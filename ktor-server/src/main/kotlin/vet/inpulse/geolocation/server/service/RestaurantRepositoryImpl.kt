package vet.inpulse.geolocation.server.service

import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.database.DatabaseFactory.query
import vet.inpulse.geolocation.server.database.RestaurantTable
import vet.inpulse.server.RestaurantRepository
import java.util.*

import kotlin.math.*

class RestaurantRepositoryImpl : RestaurantRepository {

    override suspend fun addNewRestaurant(restaurantDetails: RestaurantDetails): Boolean {
        var statement: InsertStatement<Number>? = null;
        query {
            statement = RestaurantTable.insert {
                it[id] = restaurantDetails.id
                it[name] = restaurantDetails.name
                it[latitude] = restaurantDetails.location.latitude.value
                it[longitude] = restaurantDetails.location.longitude.value
                it[streetAddress] = restaurantDetails.streetAddress
                it[phone] = restaurantDetails.phone
                it[website] = restaurantDetails.website

                if (restaurantDetails.openHours == null) return@insert
                val openHours = restaurantDetails.openHours!!

                it[openingTime] = openHours.openingTime.toString()
                it[closingTime] = openHours.closingTime.toString()
            }
        }
        return statement!!.insertedCount > 0
    }

    override suspend fun getRestaurantDetails(restaurantId: UUID): RestaurantDetails? {
        var restaurantDetails: RestaurantDetails? = null
        query {
            val result = RestaurantTable.select {
                RestaurantTable.id eq restaurantId
            }.singleOrNull() ?: return@query

            val longitude = Longitude(result[RestaurantTable.longitude])
            val latitude = Latitude(result[RestaurantTable.latitude])

            val closingTime = LocalTime.parse(result[RestaurantTable.closingTime])
            val openingTime = LocalTime.parse(result[RestaurantTable.openingTime])

            restaurantDetails = RestaurantDetails(restaurantId,
                result[RestaurantTable.name],
                Location(latitude, longitude),
                result[RestaurantTable.streetAddress],
                result[RestaurantTable.phone],
                result[RestaurantTable.website],
                OpenHours(openingTime, closingTime)
            )
        }
        return restaurantDetails
    }

    /*
        Usaria a extensão postgis com a seguinte query,
        mas utilizei um cálculo para economizar o tempo da aplicação:

        SELECT r1.id, r1.name, r1.latitude, r1.longitude
            FROM restaurants r1
            JOIN (
                SELECT r2.id, r2.location,
                       ST_Distance(
                           ST_SetSRID(ST_MakePoint(r1.longitude, r1.latitude), 4326),
                           ST_SetSRID(ST_MakePoint(r2.longitude, r2.latitude), 4326)
                       ) AS distance
                FROM restaurants r2
                CROSS JOIN LATERAL (
                    SELECT location
                    FROM restaurants
                    WHERE id = x (id de referencia)(
                ) AS ref
                WHERE r2.id <> x (outro id de referencia)
            ) AS subquery ON r1.id = subquery.id
            WHERE subquery.distance <= 2 -- distancia em KM
            ORDER BY subquery.distance;
     */
    override suspend fun getNearbyRestaurants(location: Location, batch: Int, maximumDistance: Double?): List<Restaurant> {
        val list = mutableListOf<Restaurant>()
        val nearestDistance = Float.MAX_VALUE

        query {
            RestaurantTable.selectAll().limit(batch, 0).forEach {
                val latitude = Latitude(it[RestaurantTable.latitude])
                val longitude = Longitude(it[RestaurantTable.longitude])

                val comparingLocation = Location(latitude, longitude)
                val distance = calculateDistance(location, comparingLocation)

                if (distance < nearestDistance) {
                    val id = it[RestaurantTable.id]
                    val name = it[RestaurantTable.name]

                    list.add(Restaurant(id, name, comparingLocation))
                }
            }
        }
        return list
    }

    /*
        Fórmula de Haversine

        Não desenvolvi esse cálculo, utilizei alguns exemplos e adaptei ao sistema
     */
    private fun calculateDistance(restaurantLocation: Location, comparingLocation: Location): Float {
        val lat1 = restaurantLocation.latitude.value
        val lon1 = restaurantLocation.longitude.value

        val lat2 = comparingLocation.latitude.value
        val lon2 = comparingLocation.longitude.value

        val earthRadius = 6371f

        val dLat = Math.toRadians(lat2.toDouble() - lat1.toDouble()).toFloat()
        val dLon = Math.toRadians(lon2.toDouble() - lon1.toDouble()).toFloat()

        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1.toDouble())).toFloat() * cos(Math.toRadians(lat2.toDouble())).toFloat() *
            sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

}
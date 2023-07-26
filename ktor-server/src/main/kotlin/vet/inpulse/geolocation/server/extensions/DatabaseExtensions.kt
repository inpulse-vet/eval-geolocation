package vet.inpulse.geolocation.server.extensions

import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.postgis.Point
import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.database.RestaurantTable
import java.util.UUID

fun BatchInsertStatement.parseBatch(restaurantDetails: RestaurantDetails) {
    this[RestaurantTable.id] = restaurantDetails.id
    this[RestaurantTable.name] = restaurantDetails.name
    this[RestaurantTable.location] = restaurantDetails.location.toPoint()

    this[RestaurantTable.streetAddress] = restaurantDetails.streetAddress
    this[RestaurantTable.phone] = restaurantDetails.phone
    this[RestaurantTable.website] = restaurantDetails.website

    restaurantDetails.openHours?.let { openHours ->
        this[RestaurantTable.openingTime] = openHours.openingTime.toString()
        this[RestaurantTable.closingTime] = openHours.closingTime.toString()
    }
}

fun InsertStatement<Number>.parseInsert(restaurantDetails: RestaurantDetails) {
    this[RestaurantTable.id] = restaurantDetails.id
    this[RestaurantTable.name] = restaurantDetails.name
    this[RestaurantTable.location] = restaurantDetails.location.toPoint()
    this[RestaurantTable.streetAddress] = restaurantDetails.streetAddress
    this[RestaurantTable.phone] = restaurantDetails.phone
    this[RestaurantTable.website] = restaurantDetails.website

    restaurantDetails.openHours?.let { openHours ->
        this[RestaurantTable.openingTime] = openHours.openingTime.toString()
        this[RestaurantTable.closingTime] = openHours.closingTime.toString()
    }
}

fun ResultRow.toRestaurant(): Restaurant {
    val location = this[RestaurantTable.location]
    val latitude = Latitude(location.x.toFloat())
    val longitude = Longitude(location.y.toFloat())

    return Restaurant(
        id = this[RestaurantTable.id],
        name = this[RestaurantTable.name],
        location = Location(latitude, longitude)
    )
}

fun ResultRow.toRestaurantDetails(restaurantId: UUID): RestaurantDetails {
    val rawLocation = this[RestaurantTable.location]
    val location = Location(Latitude(rawLocation.x.toFloat()), Longitude(rawLocation.y.toFloat()))

    val openingTimeStr = this[RestaurantTable.openingTime]
    val closingTimeStr = this[RestaurantTable.closingTime]

    val openingTime = openingTimeStr?.let { LocalTime.parse(it) }
    val closingTime = closingTimeStr?.let { LocalTime.parse(it) }

    val openHours = if (openingTime != null && closingTime != null) {
        OpenHours(openingTime, closingTime)
    } else null

    return RestaurantDetails(
        restaurantId, this[RestaurantTable.name], location,
        this[RestaurantTable.streetAddress], this[RestaurantTable.phone],
        this[RestaurantTable.website], openHours
    )
}


fun Location.toPoint() = Point(this.longitude.value.toDouble(), this.latitude.value.toDouble())


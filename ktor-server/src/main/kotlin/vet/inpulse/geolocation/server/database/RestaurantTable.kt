package vet.inpulse.geolocation.server.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.time

object RestaurantTable: Table("restaurants") {
    val id = uuid("id")
    val name = varchar("name", 64)
    val latitude = float("latitude")
    val longitude = float("longitude")
    val streetAddress = varchar("street_address", 128)

    val phone = varchar("phone", 15).nullable()
    val website = varchar("website", 128).nullable()

    val openingTime = varchar("opening_time", 256)
    val closingTime = varchar("closing_time", 256)

    override val primaryKey = PrimaryKey(id)
}
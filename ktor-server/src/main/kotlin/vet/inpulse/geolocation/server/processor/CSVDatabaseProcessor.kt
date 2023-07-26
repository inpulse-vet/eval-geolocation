package vet.inpulse.geolocation.server.processor

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import vet.inpulse.geolocation.*
import vet.inpulse.server.RestaurantService
import java.util.UUID

class CSVDatabaseProcessor(
    private val restaurantService: RestaurantService
) {

    private val hourPattern = """(\d{2}:\d{2})-(\d{2}:\d{2})""".toRegex()

    suspend fun processCSV() {
        restaurantService.addNewRestaurants(importFromCSV())
    }

    fun importFromCSV(): List<RestaurantDetails> {
        val resource = ClassLoader.getSystemResourceAsStream("restaurants.csv") ?: return emptyList()
        val map = CsvReader().readAllWithHeader(resource)

        return map.mapNotNull { parseRestaurantDetails(it) }
    }

    private fun parseRestaurantDetails(map: Map<String, String>): RestaurantDetails? {
        val name = map["name"] ?: return null
        if (name == "null") return null

        val id = UUID.nameUUIDFromBytes(name.toByteArray())

        val location = parseLocation(map) ?: return null
        val streetAddress = map["addr:street"] ?: return null

        val phone = map["contact:phone"]
        val website = map["contact:website"]
        val openHours = parseOpenHours(map)

        return RestaurantDetails(id, name, location, streetAddress, phone, website, openHours)
    }

    private fun parseLocation(map: Map<String, String>): Location? {
        val latitude = map["@lat"]?.toFloat() ?: return null
        val longitude = map["@lon"]?.toFloat() ?: return null

        return Location(Latitude(latitude), Longitude(longitude))
    }

    private fun parseOpenHours(map: Map<String, String>): OpenHours? {
        val openHours = map["opening_hours"] ?: return null
        val matchResult = hourPattern.find(openHours) ?: return null

        val (openingTime, closingTime) = matchResult.destructured
        return OpenHours(LocalTime.parse(openingTime), LocalTime.parse(closingTime))
    }
}
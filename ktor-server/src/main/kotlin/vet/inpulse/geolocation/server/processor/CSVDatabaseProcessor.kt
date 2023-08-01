package vet.inpulse.geolocation.server.processor

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import kotlinx.datetime.LocalTime
import vet.inpulse.geolocation.*
import java.io.File
import java.util.UUID

class CSVDatabaseProcessor {

    private val hourPattern = """(\d{2}:\d{2})-(\d{2}:\d{2})""".toRegex()

    fun importFromCSV(folder: String): List<RestaurantDetails> {
        val directory = File(folder)
        if (!directory.exists() or !directory.isDirectory) {
            throw IllegalArgumentException("$folder directory does not exist")
        }

        return directory.walk()
            .filter { it.isFile && it.extension == "csv" }
            .flatMap { it.readFileAsRestaurantList() }
            .toList()
    }

    private fun File.readFileAsRestaurantList(): List<RestaurantDetails> {
        return inputStream().use { inputStream ->
            CsvReader().readAllWithHeader(inputStream)
                .mapNotNull { parseRestaurantDetails(it)
            }
        }
    }

    private fun parseRestaurantDetails(map: Map<String, String>): RestaurantDetails? {
        val name = map["name"] ?: return null
        if (name == "null") return null

        val id = UUID.nameUUIDFromBytes(name.toByteArray())

        val location = parseLocation(map) ?: return null
        val streetAddress = map["addr:street"] ?: return null

        val phone = map["contact:phone"]
        if (phone!!.length > 19) return null

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

        var (openingTime, closingTime) = matchResult.destructured
        if (closingTime == "00:00" || closingTime >= "00:00") closingTime = "23:59";

        return OpenHours(LocalTime.parse(openingTime), LocalTime.parse(closingTime))
    }
}
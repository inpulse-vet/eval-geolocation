package vet.inpulse.geolocation.server.service

import vet.inpulse.geolocation.*
import vet.inpulse.geolocation.server.processor.CSVDatabaseProcessor
import vet.inpulse.server.RestaurantRepository
import vet.inpulse.server.RestaurantService
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.log
import kotlin.time.measureTime
import kotlin.time.measureTimedValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RestaurantServiceImpl(
    private val restaurantRepository: RestaurantRepository,
    private val csvDatabaseProcessor: CSVDatabaseProcessor
): RestaurantService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Throws(ApplicationException::class)
    override suspend fun loadDataFromCSVFolder(folder: String) {
        logger.info("Check if we need to import restaurants from CSV.")
        val totalBefore = restaurantRepository.getTotalNumberOfRestaurants()
        if (totalBefore < 100153 /* hardcode number for now */) {
            val time = measureTime {
                restaurantRepository.addNewRestaurants(csvDatabaseProcessor.importFromCSV(folder))
            }
            val totalAfter = restaurantRepository.getTotalNumberOfRestaurants()
            logger.info("Imported $totalAfter restaurants from CSV in $time")
        } else {
            logger.info("Restaurants in database: $totalBefore.")
        }
    }

    @Throws(ApplicationException::class)
    override suspend fun addNewRestaurants(restaurants: List<RestaurantDetails>) {
        if (restaurants.isEmpty()) {
            throw ApplicationException(error = Error.MALFORMED_DATA)
        }
        restaurantRepository.addNewRestaurants(restaurants)
    }

    @Throws(ApplicationException::class)
    override suspend fun addRestaurant(restaurantDetails: RestaurantDetails) {
        if (restaurantDetails.name.isEmpty() or restaurantDetails.streetAddress.isEmpty()) {
            throw ApplicationException(error = Error.MALFORMED_DATA)
        }
        restaurantRepository.addNewRestaurant(restaurantDetails)
    }

    @Throws(ApplicationException::class)
    override suspend fun getRestaurantDetails(restaurantId: String): RestaurantDetails? {
        var id: UUID

        try {
            id = UUID.fromString(restaurantId)
        } catch (exception: IllegalArgumentException) {
            throw ApplicationException(Error.INVALID_ID, exception)
        }

        return restaurantRepository.getRestaurantDetails(id)
    }

    @Throws(ApplicationException::class)
    override suspend fun getNearbyRestaurants(
        location: Location, batch: Int, maximumDistance: Double?
    ): List<Restaurant> {
        var list: List<Restaurant>
        try {
            list = restaurantRepository.getNearbyRestaurants(location, batch, maximumDistance)
        } catch (exception: IllegalArgumentException) {
            throw ApplicationException(Error.MALFORMED_INPUT)
        }
        return list
    }
}
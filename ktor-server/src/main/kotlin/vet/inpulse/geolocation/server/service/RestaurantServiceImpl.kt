package vet.inpulse.geolocation.server.service

import vet.inpulse.geolocation.*
import vet.inpulse.server.RestaurantRepository
import vet.inpulse.server.RestaurantService
import java.lang.IllegalArgumentException
import java.util.*

class RestaurantServiceImpl(
    private val restaurantRepository: RestaurantRepository
): RestaurantService {

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
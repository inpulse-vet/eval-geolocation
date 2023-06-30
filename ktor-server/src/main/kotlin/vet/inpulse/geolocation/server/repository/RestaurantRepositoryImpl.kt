package vet.inpulse.geolocation.server.repository

import vet.inpulse.geolocation.*
import vet.inpulse.server.RestaurantRepository
import vet.inpulse.server.RestaurantService
import java.lang.IllegalArgumentException
import java.util.*

class RestaurantRepositoryImpl(
    private val restaurantService: RestaurantService
): RestaurantRepository {

    @Throws(ApplicationException::class)
    override suspend fun addRestaurant(restaurantDetails: RestaurantDetails) {
        if (restaurantDetails.name.isEmpty() or restaurantDetails.streetAddress.isEmpty()) {
            throw ApplicationException(error = Error.MALFORMED_DATA)
        }
        restaurantService.addNewRestaurant(restaurantDetails)
    }

    @Throws(ApplicationException::class)
    override suspend fun getRestaurantDetails(restaurantId: String): RestaurantDetails? {
        var id: UUID

        try {
            id = UUID.fromString(restaurantId)
        } catch (exception: IllegalArgumentException) {
            throw ApplicationException(Error.INVALID_ID, exception)
        }

        return restaurantService.getRestaurantDetails(id)
    }

    @Throws(ApplicationException::class)
    override suspend fun getNearbyRestaurants(
        location: Location, batch: Int, maximumDistance: Double?
    ): List<Restaurant> {
        var list: List<Restaurant>
        try {
            list = restaurantService.getNearbyRestaurants(location, batch, maximumDistance)
        } catch (exception: IllegalArgumentException) {
            throw ApplicationException(Error.MALFORMED_INPUT)
        }
        return list
    }

}
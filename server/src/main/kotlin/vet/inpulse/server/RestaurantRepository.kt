package vet.inpulse.server

import vet.inpulse.geolocation.ApplicationException
import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Restaurant
import vet.inpulse.geolocation.RestaurantDetails

interface RestaurantRepository {

    @Throws(ApplicationException::class)
    suspend fun addRestaurant(restaurantDetails: RestaurantDetails)

    @Throws(ApplicationException::class)
    suspend fun getRestaurantDetails(restaurantId: String): RestaurantDetails?

    @Throws(ApplicationException::class)
    suspend fun getNearbyRestaurants(location: Location, batch: Int, maximumDistance: Double?): List<Restaurant>
}
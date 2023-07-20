package vet.inpulse.server

import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Restaurant
import vet.inpulse.geolocation.RestaurantDetails
import java.util.*

interface RestaurantRepository {

    suspend fun addNewRestaurant(restaurantDetails: RestaurantDetails): Boolean

    suspend fun getRestaurantDetails(restaurantId: UUID): RestaurantDetails?

    suspend fun getNearbyRestaurants(location: Location, batch: Int, maximumDistance: Double?): List<Restaurant>
}
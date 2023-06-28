package vet.inpulse.geolocation.client
import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Restaurant
import vet.inpulse.geolocation.RestaurantDetails
import java.util.UUID

/**
 * Main interface to implement client functionality
 */
interface ClientApi {
    suspend fun listRestaurants(location: Location, numberOfResults: Int, rangeKm: Float? = null): Result<List<Restaurant>>
    suspend fun getRestaurantDetails(restaurantId: UUID): Result<RestaurantDetails>
    suspend fun insertRestaurant(restaurantDetails: RestaurantDetails): Result<RestaurantDetails>
}
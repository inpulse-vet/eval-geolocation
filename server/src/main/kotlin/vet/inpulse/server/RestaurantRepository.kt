package vet.inpulse.server

import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Restaurant
import vet.inpulse.geolocation.RestaurantDetails
import java.util.*

interface RestaurantRepository {

    /**
     * Returns the total amount of restaurants in the database.
     */
    suspend fun getTotalNumberOfRestaurants(): Long

    /**
     * Inserts a list of new restaurants into the database in a single transaction.
     *
     * @param restaurants The list of [RestaurantDetails] objects to be inserted.
     */
    suspend fun addNewRestaurants(restaurants: List<RestaurantDetails>)

    /**
     * Inserts a new restaurant into the database.
     *
     * @param restaurantDetails The details of the restaurant.
     * @return True if the restaurant was inserted successfully, false otherwise.
     */
    suspend fun addNewRestaurant(restaurantDetails: RestaurantDetails): Boolean

    /**
     * Gets the details of a restaurant.
     *
     * @param restaurantId The ID of the restaurant.
     * @return The details of the restaurant, or null if the restaurant does not exist.
     */
    suspend fun getRestaurantDetails(restaurantId: UUID): RestaurantDetails?

    /**
     * Gets a list of nearby restaurants.
     *
     * @param location The [Location] to search from.
     * @param batch The number of restaurants to return.
     */
    suspend fun getNearbyRestaurants(location: Location, batch: Int, maximumDistance: Double?): List<Restaurant>
}
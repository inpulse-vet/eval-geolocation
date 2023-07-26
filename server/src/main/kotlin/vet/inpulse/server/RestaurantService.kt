package vet.inpulse.server

import vet.inpulse.geolocation.ApplicationException
import vet.inpulse.geolocation.Location
import vet.inpulse.geolocation.Restaurant
import vet.inpulse.geolocation.RestaurantDetails

interface RestaurantService {

    /**
     * Inserts a list of new restaurants into the database in a single transaction.
     *
     * @param restaurants The list of [RestaurantDetails] objects to be inserted.
     */
    suspend fun addNewRestaurants(restaurants: List<RestaurantDetails>)

    /**
     * Adds a new restaurant to the database.
     *
     * @param restaurantDetails The details of the restaurant.
     * @throws ApplicationException If the data is malformed.
     */
    @Throws(ApplicationException::class)
    suspend fun addRestaurant(restaurantDetails: RestaurantDetails)

    /**
     * Gets the details of a restaurant.
     *
     * @param restaurantId The ID of the restaurant.
     * @throws ApplicationException If the ID is invalid.
     */
    @Throws(ApplicationException::class)
    suspend fun getRestaurantDetails(restaurantId: String): RestaurantDetails?

    /**
     * Gets a list of nearby restaurants.
     *
     * @param location The location to search from.
     * @param batch The number of restaurants to return.
     * @param maximumDistance The maximum distance (km) from the location to search.
     *
     * @throws ApplicationException If the input is malformed.
     */
    @Throws(ApplicationException::class)
    suspend fun getNearbyRestaurants(location: Location, batch: Int, maximumDistance: Double?): List<Restaurant>
}
package vet.inpulse.geolocation

import java.util.UUID

/**
 * Detailed information about a Restaurant.
 */
data class RestaurantDetails(
    val id: UUID,
    val name: String,
    val location: Location,
    val streetAddress: String,
    val phone: String?,
    val website: String?,
    val openHours: OpenHours?,
)

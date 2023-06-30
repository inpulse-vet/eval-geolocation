package vet.inpulse.geolocation

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import vet.inpulse.geolocation.serialize.UUIDSerializer
import java.util.UUID

/**
 * Detailed information about a Restaurant.
 */
@Serializable
data class RestaurantDetails(

    @Serializable(with = UUIDSerializer::class)
    val id: UUID,

    val name: String,
    val location: Location,
    val streetAddress: String,
    val phone: String?,
    val website: String?,
    val openHours: OpenHours?,
)

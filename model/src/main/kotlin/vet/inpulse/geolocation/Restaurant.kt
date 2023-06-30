package vet.inpulse.geolocation

import kotlinx.serialization.Serializable
import vet.inpulse.geolocation.serialize.UUIDSerializer
import java.util.UUID

/**
 * A compacted representation of a `Restaurant`.
 */
@Serializable
data class Restaurant(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val location: Location
)
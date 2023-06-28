package vet.inpulse.geolocation

import java.util.UUID

/**
 * A compacted representation of a `Restaurant`.
 */
data class Restaurant(
    val id: UUID,
    val name: String,
    val location: Location
)
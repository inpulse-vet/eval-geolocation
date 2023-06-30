package vet.inpulse.geolocation

import kotlinx.serialization.Serializable

/**
 * Latitude in degrees
 */
@JvmInline
@Serializable
value class Latitude(val value: Float) {
    init {
        require(value in -90f..90f)
    }
}

/**
 * Longitude in degrees
 */
@JvmInline
@Serializable
value class Longitude(val value: Float) {
    init {
        require(value in -180f..180f)
    }
}

/**
 * Combines latitude and longitude.
 */
@Serializable
data class Location(
    val latitude: Latitude,
    val longitude: Longitude,
)
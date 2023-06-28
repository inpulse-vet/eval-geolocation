package vet.inpulse.geolocation

/**
 * Latitude in degrees
 */
@JvmInline
value class Latitude(val value: Float) {
    init {
        require(value in -90f..90f)
    }
}

/**
 * Longitude in degrees
 */
@JvmInline
value class Longitude(val value: Float) {
    init {
        require(value in -180f..180f)
    }
}

/**
 * Combines latitude and longitude.
 */
data class Location(
    val latitude: Latitude,
    val longitude: Longitude,
)
package vet.inpulse.geolocation

import kotlinx.datetime.LocalTime

/**
 * Represents a restaurant opening and closing times.
 * Can use this data to display if restaurant is currently opened on closed.
 */
data class OpenHours(
    val openingTime: LocalTime,
    val closingTime: LocalTime,
) {
    init {
        require(closingTime > openingTime) {
            "`closingTime` must be strictly later than `openingTime`. `closingTime`: $closingTime, `openingTime`: $openingTime"
        }
    }
}
package vet.inpulse.geolocation

/**
 * Error coming from backend server.
 */
enum class Error {
    INVALID_ID,
    MALFORMED_INPUT,
    MALFORMED_DATA,
    UNAUTHORIZED,
    UNKNOWN,
}

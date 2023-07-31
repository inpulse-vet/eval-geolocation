package vet.inpulse.server.health

enum class AvailabilityState(val readinessCode: Int) {
    STARTING(503),
    STARTED(503),
    READY(200),
    GRACEFUL_SHUTDOWN(503),
}

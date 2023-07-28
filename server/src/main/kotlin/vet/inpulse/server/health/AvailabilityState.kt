package vet.inpulse.server.health

/*
 *  READINESS (READY)
 *  HEALTHCHECK (STARTED, READY),
 *  GRACEFUL_SHUTDOWN (STOP_PREPARING, STOPPING)
 */
enum class AvailabilityState(val readinessCode: Int, val healthCode: Int) {
    STARTING(503, 503),
    STARTED(503, 200),
    READY(200, 200),
    GRACEFUL_SHUTDOWN(503, 503),
    BROKEN(503, 503)
}

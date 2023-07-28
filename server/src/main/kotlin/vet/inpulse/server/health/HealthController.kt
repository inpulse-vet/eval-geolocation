package vet.inpulse.server.health

interface HealthController {

    fun setAvailabilityState(state: AvailabilityState)

    suspend fun getAvailabilityState(): AvailabilityState

    suspend fun getCurrentReadinessCode(): Int

    suspend fun getCurrentHealthCode(): Int
}
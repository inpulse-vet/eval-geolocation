package vet.inpulse.geolocation.server.health

import vet.inpulse.server.health.AvailabilityState
import vet.inpulse.server.health.DatabaseHealthRepository
import vet.inpulse.server.health.HealthController
import java.util.concurrent.atomic.AtomicReference

class HealthControllerImpl(private val databaseHealthRepository: DatabaseHealthRepository) : HealthController {

    private val availabilityState = AtomicReference(AvailabilityState.STARTING)

    override fun setAvailabilityState(state: AvailabilityState) {
        availabilityState.set(state)
    }

    override suspend fun getAvailabilityState(): AvailabilityState {
        if (!databaseHealthRepository.checkDatabaseHealth()) availabilityState.set(AvailabilityState.BROKEN)

        return availabilityState.get()
    }

    override suspend fun getCurrentReadinessCode(): Int = getAvailabilityState().readinessCode


    override suspend fun getCurrentHealthCode(): Int = getAvailabilityState().healthCode

}
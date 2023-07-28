package vet.inpulse.geolocation.server.health

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getKoin
import vet.inpulse.server.health.AvailabilityState
import vet.inpulse.server.health.HealthController

val MonitoringPlugin = createApplicationPlugin(name = "MonitoringPlugin") {
    val healthController = application.getKoin().get<HealthController>()

    on(MonitoringEvent(ApplicationStarting)) {
        healthController.setAvailabilityState(AvailabilityState.STARTING)
    }

    on(MonitoringEvent(ApplicationStarted)) {
        healthController.setAvailabilityState(AvailabilityState.STARTED)
    }

    on(MonitoringEvent(ServerReady)) {
        healthController.setAvailabilityState(AvailabilityState.READY)
    }

    on(MonitoringEvent(ApplicationStopPreparing)) {
        healthController.setAvailabilityState(AvailabilityState.GRACEFUL_SHUTDOWN)
    }

    application.intercept(ApplicationCallPipeline.Call) {
        if (healthController.getAvailabilityState() == AvailabilityState.GRACEFUL_SHUTDOWN) {
            call.respond(HttpStatusCode.ServiceUnavailable, "Server stopping")
            finish()
        }

        this.application.routing {
            authenticate("auth-basic") {
                get("/health") {
                    val rawCode = healthController.getCurrentHealthCode()
                    call.respond(HttpStatusCode.fromValue(rawCode))
                }

                get("/ready") {
                    val rawCode = healthController.getCurrentReadinessCode()
                    call.respond(rawCode)
                }
            }
        }
    }
}

package vet.inpulse.geolocation.server.health

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.atomic.AtomicReference

private val availabilityState = AtomicReference(AvailabilityState.STARTING)

class PluginConfiguration(var checkHealthAction: (suspend () -> Boolean?) = { false })

val MonitoringPlugin = createApplicationPlugin(name = "MonitoringPlugin", createConfiguration = ::PluginConfiguration) {
    val checkHealthAction = pluginConfig.checkHealthAction

    on(MonitoringEvent(ApplicationStarting)) {
        availabilityState.set(AvailabilityState.STARTING)
    }

    on(MonitoringEvent(ApplicationStarted)) {
        availabilityState.set(AvailabilityState.STARTED)
    }

    on(MonitoringEvent(ServerReady)) {
        availabilityState.set(AvailabilityState.READY)
    }

    on(MonitoringEvent(ApplicationStopPreparing)) {
        availabilityState.set(AvailabilityState.GRACEFUL_SHUTDOWN)
    }

    application.intercept(ApplicationCallPipeline.Call) {
        if (availabilityState.get() == AvailabilityState.GRACEFUL_SHUTDOWN) {
            call.respond(HttpStatusCode.ServiceUnavailable, "Server stopping")
            finish()
        }

        this.application.routing {
            authenticate("auth-basic") {
                get("/health") {
                    try {
                        checkHealthAction.invoke()
                        call.respond(HttpStatusCode.OK, "Database is available")
                    } catch(exception: Exception) {
                        call.respond(HttpStatusCode.ServiceUnavailable, "Database is not available")
                    }
                }

                get("/ready") {
                    val rawCode = availabilityState.get()
                    call.respond(HttpStatusCode.fromValue(rawCode.readinessCode), rawCode.toString())
                }
            }
        }
    }
}

enum class AvailabilityState(val readinessCode: Int) {
    STARTING(503),
    STARTED(503),
    READY(200),
    GRACEFUL_SHUTDOWN(503),
}

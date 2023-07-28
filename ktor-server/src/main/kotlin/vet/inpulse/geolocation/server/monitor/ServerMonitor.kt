package vet.inpulse.geolocation.server.monitor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import vet.inpulse.server.RestaurantService
import java.util.concurrent.atomic.AtomicBoolean

private val serverStopAction = AtomicBoolean(false)
private val serverReadyAction = AtomicBoolean(true)

private fun Application.configureStopMonitor() = environment.monitor.subscribe(ApplicationStopPreparing) {
    serverStopAction.set(true)

    runBlocking {
        delay(5000)
    }
}

private fun Application.configureShutdownServerMonitoring() = intercept(ApplicationCallPipeline.Call) {
    if (!serverStopAction.get()) {
        proceed()
        return@intercept
    }

    call.respondText("Service unavailable, server is stopping", status = HttpStatusCode.ServiceUnavailable)
    finish()
}
private fun Application.configureReadinessMonitoring() = environment.monitor.subscribe(ServerReady) {
    serverReadyAction.set(true)
}

private fun Application.routerReadyMonitoring() = intercept(ApplicationCallPipeline.Call) {
    if (serverReadyAction.get()) {
        proceed()
        return@intercept
    }

    call.respondText("Service unavailable, server is starting", status = HttpStatusCode.ServiceUnavailable)
    finish()
}

fun Route.getReadyRouting() {
    val restaurantService by inject<RestaurantService>()

    get("/ready") {
        val statusCode = if (serverReadyAction.get() && restaurantService.checkDatabaseStatus()!!)
            HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

        call.respond(statusCode)
    }
}

fun Application.configureServerMonitoring() {
    configureShutdownServerMonitoring()
    configureReadinessMonitoring()
    routerReadyMonitoring()
    configureStopMonitor()
}

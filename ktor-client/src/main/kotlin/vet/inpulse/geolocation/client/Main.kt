package vet.inpulse.geolocation.client

import io.ktor.http.*

fun main(args: Array<String>) {
    val client = KtorClientApiImpl(Url("http://localhost/"), "user", "password")
}
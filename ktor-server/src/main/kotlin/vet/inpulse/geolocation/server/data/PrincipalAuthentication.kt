package vet.inpulse.geolocation.server.data

import io.ktor.server.auth.*

data class PrincipalAuthentication(val userId: String) : Principal

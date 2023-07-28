package vet.inpulse.server.health

interface DatabaseHealthRepository {

    suspend fun checkDatabaseHealth(): Boolean
}
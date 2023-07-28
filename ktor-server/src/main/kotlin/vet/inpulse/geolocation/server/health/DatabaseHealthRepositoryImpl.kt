package vet.inpulse.geolocation.server.health

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import vet.inpulse.server.health.DatabaseHealthRepository

class DatabaseHealthRepositoryImpl: DatabaseHealthRepository {

    override suspend fun checkDatabaseHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            transaction {
                exec("SELECT 1") { it.next() }
            }
            true
        } catch (exception: Exception) {
            false
        }
    }
}
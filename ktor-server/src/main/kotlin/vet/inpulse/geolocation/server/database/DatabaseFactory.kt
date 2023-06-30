package vet.inpulse.geolocation.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(hikariConfiguration())

        transaction {
            SchemaUtils.create(RestaurantTable)
        }
    }

    private fun hikariConfiguration(): HikariDataSource {
        val config = HikariConfig()

        // switch to environment variables
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/geolocation"

        config.username = "mathews"
        config.password = "1234"

        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }
}
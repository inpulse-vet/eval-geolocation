package vet.inpulse.geolocation.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(jdbcUrl: String, username: String, password: String): Database {
        val database = Database.connect(hikariConfiguration(jdbcUrl, username, password))

        transaction {
            SchemaUtils.create(RestaurantTable)
        }
        return database;
    }

    private fun hikariConfiguration(jdbcUrl: String, username: String, password: String): HikariDataSource {
        val config = HikariConfig().apply {
            this.driverClassName = "org.postgresql.Driver"
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password

            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }
}
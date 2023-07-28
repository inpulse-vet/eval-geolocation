package vet.inpulse.geolocation.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun createDatabaseConnection(configuration: Configuration) {
        dataSource = HikariDataSource(hikariConfiguration(configuration))
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(RestaurantTable)
        }
    }

    private fun hikariConfiguration(configuration: Configuration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = configuration.url
            username = configuration.user
            password = configuration.password
            maximumPoolSize = 3
            connectionTimeout = 1000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
    }
}

data class Configuration(
    val url: String,
    val user: String,
    val password: String
)
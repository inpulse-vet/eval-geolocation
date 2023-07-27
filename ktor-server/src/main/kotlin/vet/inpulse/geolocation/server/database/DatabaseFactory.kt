package vet.inpulse.geolocation.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(configuration: Configuration) {
        val dataSource = HikariDataSource(hikariConfiguration(configuration))
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(RestaurantTable)
        }
    }

    /*
    System.getenv("POSTGRES_URL")
    System.getenv("POSTGRES_USER")
    System.getenv("POSTGRES_PASSWORD")
     */

    private fun hikariConfiguration(configuration: Configuration): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = configuration.url
            username = configuration.user
            password = configuration.password
            maximumPoolSize = 3
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
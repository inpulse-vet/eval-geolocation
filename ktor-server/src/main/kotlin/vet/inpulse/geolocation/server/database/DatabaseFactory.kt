package vet.inpulse.geolocation.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val dataSource = HikariDataSource(hikariConfiguration())
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(RestaurantTable)
        }
    }

    private fun hikariConfiguration(): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = System.getenv("POSTGRES_URL")
            username = System.getenv("POSTGRES_USER")
            password = System.getenv("POSTGRES_PASSWORD")
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
    }
}
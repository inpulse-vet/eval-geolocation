package vet.inpulse.geolocation.server.database

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    private val configurationFile: Config = ConfigFactory.load()

    fun init(config: DatabaseConfig? = configurationFile.readFromProperties()) {
        val databaseConfig = config ?: DatabaseConfig()
        val dataSource = HikariDataSource(hikariConfiguration(databaseConfig))

        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(RestaurantTable)
        }
    }

    private fun Config.readFromProperties(): DatabaseConfig {
        return DatabaseConfig(
              getString("database.jdbcUrl"),
              getString("database.username"),
              getString("database.password")
        )
    }

    private fun hikariConfiguration(config: DatabaseConfig): HikariConfig {
        return HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
    }
}

data class DatabaseConfig(
      val jdbcUrl: String = "jdbc:postgresql://localhost:5432/testDb",
      val username: String = "testUser",
      val password: String = "testPassword"
)
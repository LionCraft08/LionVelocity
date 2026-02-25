package dev.lionk.lionVelocity.sql

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.data.Config
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import java.io.FileReader
import java.sql.Connection
import java.sql.ResultSet

class SQLManager(
    private val driver: String,
    private val host: String,
    private val port: Int,
    private val database: String,
    private val user: String,
    private val pass: String
) {
    private var dataSource: HikariDataSource? = null

    /**
     * Initializes the connection pool
     */
    fun connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            Class.forName("org.mariadb.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:$driver://$host:$port/$database"
            username = user
            password = pass
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            maximumPoolSize = 10
        }
        dataSource = HikariDataSource(config)
    }

    /**
     * Closes the connection pool
     */
    fun disconnect() {
        dataSource?.close()
    }

    /**
     * Executes an update (INSERT, UPDATE, DELETE)
     */
    fun executeUpdate(query: String, vararg params: Any): Int {
        return dataSource?.connection?.use { conn ->
            conn.prepareStatement(query).use { stmt ->
                params.forEachIndexed { index, param ->
                    stmt.setObject(index + 1, param)
                }
                stmt.executeUpdate()
            }
        } ?: 0
    }

    /**
     * Executes a query (SELECT) and returns a result via a callback
     */
    fun <T> executeQuery(query: String, mapper: (ResultSet) -> T, vararg params: Any): T? {
        return dataSource?.connection?.use { conn ->
            conn.prepareStatement(query).use { stmt ->
                params.forEachIndexed { index, param ->
                    stmt.setObject(index + 1, param)
                }
                stmt.executeQuery().use { rs ->
                    if (rs.next()) mapper(rs) else null
                }
            }
        }
    }
    /**
     * Executes a query for Lists (SELECT) and returns a result via a callback
     */
    fun <T> executeListQuery(query: String, mapper: (ResultSet) -> T, vararg params: Any): List<T>? {
        return dataSource?.connection?.use { conn ->
            conn.prepareStatement(query).use { stmt ->
                params.forEachIndexed { index, param ->
                    stmt.setObject(index + 1, param)
                }
                stmt.executeQuery().use { rs ->
                    var list = ArrayList<T>()
                    while (rs.next()) list.add(mapper(rs))
                    return list
                }
            }
        }
    }

    companion object {
        val config : JsonObject=
            if (Config.getValue("sql-configuration") != null){
                Config.getValue("sql-configuration") as JsonObject
            } else {
                val jo = JsonParser.parseString("{\n" +
                        "    \"driver\": \"mariadb\",\n" +
                        "    \"host\": \"localhost\",\n" +
                        "    \"port\": 3306,\n" +
                        "    \"database\": \"velocity_playerdata\",\n" +
                        "    \"user\": \"velocity\",\n" +
                        "    \"password\": \"11223344\"\n" +
                        "  }") as JsonObject
                Config.getJO().add("sql-configuration", jo)
                jo
            }

        val instance = SQLManager(
            config.get("driver").asString,
            config.get("host").asString,
            config.get("port").asInt,
            config.get("database").asString,
            config.get("user").asString,
            config.get("password").asString
        )
    }
}
package dev.lionk.lionVelocity.playerManagement

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import de.lioncraft.lionapi.velocity.data.PlayerConfiguration
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.sql.SQLManager
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object PlayerConfigCache {

//SQL Mappings

    // Define types for Gson parsing
    private val gson = Gson()
    private val permissionsType = object : TypeToken<ArrayList<String>>() {}.type
    private val dataType = object : TypeToken<HashMap<String, JsonElement>>() {}.type

    /**
     * Saves or Updates a PlayerConfiguration in the database
     */
    fun savePlayerConfig(config: PlayerConfiguration) {
        val query = """
        INSERT INTO player_configs (uuid, timestamp, is_operator, last_online, auto_server_switch, permissions, extra_data)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE 
            timestamp = VALUES(timestamp),
            is_operator = VALUES(is_operator),
            last_online = VALUES(last_online),
            auto_server_switch = VALUES(auto_server_switch),
            permissions = VALUES(permissions),
            extra_data = VALUES(extra_data)
    """.trimIndent()
        val future = CompletableFuture<Boolean>().completeAsync {
            try {
                SQLManager.instance.executeUpdate(
                    query,
                    config.uuid.toString(),
                    config.timestamp,
                    config.isOperator,
                    config.lastOnline,
                    config.autoServerSwitch,
                    gson.toJson(config.permissions), // Serialize List to JSON
                    gson.toJson(config.data)         // Serialize Map to JSON
                )
            }catch (e: Exception) {
                e.printStackTrace()
                return@completeAsync false
            }
            return@completeAsync true
        }
    }

    /**
     * Reads a PlayerConfiguration from the database by UUID
     */
    fun loadPlayerConfig(uuid: UUID): CompletableFuture<PlayerConfiguration?> {
        val query = "SELECT * FROM player_configs WHERE uuid = ?"

        return CompletableFuture<PlayerConfiguration?>().completeAsync {
            return@completeAsync SQLManager.instance.executeQuery(query, { rs ->
                PlayerConfiguration(
                    rs.getLong("timestamp"),
                    rs.getBoolean("is_operator"),
                    rs.getLong("last_online"),
                    rs.getBoolean("auto_server_switch"),
                    gson.fromJson(rs.getString("permissions"), permissionsType),
                    gson.fromJson(rs.getString("extra_data"), dataType),
                    UUID.fromString(rs.getString("uuid"))
                )
            }, uuid.toString())
        }
    }


//Cache

    private val cache = ConcurrentHashMap<UUID, PlayerConfiguration>()

    fun getCachedPlayerConfig(uuid: UUID): PlayerConfiguration? {
        return cache[uuid]
    }
    fun getPlayerConfig(uuid: UUID): CompletableFuture<PlayerConfiguration?> {
        return CompletableFuture<PlayerConfiguration>().completeAsync {
            if (cache[uuid] != null)
                return@completeAsync cache[uuid]
            else return@completeAsync loadPlayerConfig(uuid).get()
        }
    }
    fun getOrCreatePlayerConfig(uuid: UUID): CompletableFuture<PlayerConfiguration> {
        return CompletableFuture<PlayerConfiguration>().completeAsync {
            val config = getPlayerConfig(uuid).get()
            if (config != null)
                return@completeAsync config
            else{
                cache.putIfAbsent(uuid, PlayerConfiguration(uuid))
                return@completeAsync cache[uuid]!!
            }
        }
    }

    /**
     * Removes every cache entry for disconnected players.
     */
    fun cleanCache(){
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
            cache.entries.forEach {
                val player = LionVelocity.instance.server.getPlayer(it.key)
                if (player == null)
                    saveAndDeletePlayerConfig(it.key)
            }
        }).schedule()
    }

    /**
     * Saves and deletes every cache entry. Should only be called when no player is connected.
     */
    fun shutdownCache(){
        for (entry in cache){
            saveAndDeletePlayerConfig(entry.key)
        }
    }

    fun saveUpdateCachedPlayer(pc: PlayerConfiguration){
        val oldPC = getCachedPlayerConfig(pc.uuid)
        if (oldPC == null || oldPC.timestamp <= pc.timestamp) {
            cache[pc.uuid] = pc
        }
    }

    fun saveAndDeletePlayerConfig(uuid: UUID) {
        savePlayerConfig(uuid)
        cache.remove(uuid)
    }

    fun savePlayerConfig(uuid: UUID){
        val config = getCachedPlayerConfig(uuid)
        if (config != null)
            savePlayerConfig(config)
    }


}
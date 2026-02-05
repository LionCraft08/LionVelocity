package dev.lionk.lionVelocity.playerManagement

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.velocity.data.PlayerConfiguration
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.AbstractConnection
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.playerManagement.mojang.PlayerCache
import java.io.*
import java.nio.file.Files
import java.time.Duration
import java.util.*

object PlayerDataManager {

    val file: File = File(LionVelocity.instance.dataDirectory.toFile(), "playerdata.json")

    val gson: Gson = Gson()
    
    var playerData: HashMap<UUID, PlayerData>? = null

    /**
     * This function exists to convert old data into new, database-stored data.
     */
    fun init() {
        try {
            val typeOfMap =
                TypeToken.getParameterized(HashMap::class.java, UUID::class.java, PlayerData::class.java).type
            playerData = gson.fromJson<HashMap<UUID, PlayerData>?>(FileReader(file), typeOfMap)
            if (playerData != null){
                playerData!!.forEach {
                    LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
                        val pc = PlayerConfigCache.getOrCreatePlayerConfig(it.value.uuid).get()
                        pc.isOperator = it.value.isOP
                        pc.lastOnline = it.value.lastOnline
                        pc.data = it.value.data
                        pc.timestamp = System.currentTimeMillis()
                    }).schedule()
                }
            }
            save()
        } catch (_: FileNotFoundException) {/*ignore when no old data exists*/}
        catch (_:NoSuchElementException){}

    }

    fun save() {
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
            Files.delete(file.toPath())
        }).delay(Duration.ofSeconds(1)).schedule()

    }
    fun sendPlayerData(player: Player){
        sendPlayerData(player, player.currentServer.get().server)
    }
    fun sendPlayerData(player: Player, registeredServer: RegisteredServer){
        sendPlayerData(registeredServer, PlayerConfigCache.getOrCreatePlayerConfig(player.uniqueId).get())
    }

    fun sendPlayerData(registeredServer: RegisteredServer, playerData: PlayerConfiguration){
        BackendServerManager.getConnection(registeredServer)?.sendMessage(TransferrableObject("lionapi_playerdata")
            .addValue("data", playerData.toString())
            .addValue("uuid", playerData.uuid.toString()))
    }

    fun sendPlayerDataUpdate(playerData: PlayerConfiguration, key: String, value: Any){
        BackendServerManager.getConnections().forEach {
            connection -> if (connection.isConnected()) connection.sendMessage(TransferrableObject("lionapi_playerdata_update")
            .addValue("key", key)
            .addValue("data", value.toString())
            .addValue("player", playerData.uuid.toString()))
        }
    }


}
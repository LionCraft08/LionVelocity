package dev.lionk.lionVelocity.playerManagement

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import java.io.*
import java.util.*

object PlayerDataManager {

    val file: File = File(LionVelocity.instance.dataDirectory.toFile(), "playerdata.json")

    val gson: Gson = Gson()
    
    var playerData: HashMap<UUID, PlayerData>? = null
    var playerMapping = HashMap<String, UUID>()


    fun init() {
        try {
            val typeOfMap =
                TypeToken.getParameterized(HashMap::class.java, UUID::class.java, PlayerData::class.java).type
            playerData = gson.fromJson<HashMap<UUID, PlayerData>?>(FileReader(file), typeOfMap)
            if (playerData == null) playerData = HashMap()

            playerData!!.forEach { (key, value) ->
                if (value.name != null){
                    playerMapping[value.name!!] = key
                }
            }
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }

    }

    fun save() {
        try {
            FileWriter(file).use { writer ->
                // Convert the HashMap object to a JSON string and write to file
                gson.toJson(playerData, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun getPlayerData(uuid: UUID): PlayerData {
        if (!playerData!!.containsKey(uuid)) playerData!![uuid] = PlayerData(uuid)
        return playerData!![uuid]!!
    }

    fun setPlayerData(uuid: UUID, playerData: PlayerData){
        PlayerDataManager.playerData!![uuid] = playerData
        if (playerData.name != null)
            playerMapping[playerData.name!!] = uuid
    }

    fun sendPlayerData(uuid: UUID){
        sendPlayerData(LionVelocity.instance.server.getPlayer(uuid).get())
    }
    fun sendPlayerData(player: Player){
        sendPlayerData(player.currentServer.get().server, getPlayerData(player.uniqueId))
    }
    fun sendPlayerData(registeredServer: RegisteredServer, playerData: PlayerData){
        BackendServerManager.getConnection(registeredServer)?.sendMessage(TransferrableObject("lionapi_playerdata")
            .addValue("data", playerData.toString()))
    }

    fun sendPlayerDataUpdate(playerData: PlayerData, key: String, value: Any){
        BackendServerManager.getConnections().forEach {
            connection -> if (connection.isConnected()) connection.sendMessage(TransferrableObject("lionapi_playerdata_update")
            .addValue("key", key)
            .addValue("data", value.toString())
            .addValue("player", playerData.uuid.toString()))
        }
    }


}
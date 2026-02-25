package dev.lionk.lionVelocity.playerManagement

import com.velocitypowered.api.proxy.Player
import dev.lionk.lionVelocity.LionVelocity
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object AutoConnectionManager {
    private val queuedConnections = HashMap<String, MutableList<UUID>>()
    fun addNewConnection(player: Player){
        addNewConnection(player.currentServer.getOrNull()?.serverInfo?.name?:"null", player.uniqueId)
    }
    fun addNewConnection(serverName: String, uuid: UUID){
        val config = PlayerConfigCache.getCachedPlayerConfig(uuid)?:return
        if (config.autoServerSwitch) {
            if (!queuedConnections.containsKey(serverName)) {
                queuedConnections[serverName] = arrayListOf<UUID>()
            }
            queuedConnections[serverName]!!.add(uuid)
        }
    }
    fun connectIfAvailable(serverName: String){
        if(queuedConnections.containsKey(serverName)){
            println("Attempting to connect players to $serverName")
            queuedConnections[serverName]?.forEach {
                if(PlayerConfigCache.getCachedPlayerConfig(it)?.autoServerSwitch?:false)
                    LionVelocity.instance.server.getPlayer(it).getOrNull()?.saveQueueReconnect(LionVelocity.instance.server.getServer(serverName).getOrNull()!!)
            }
            queuedConnections[serverName]?.clear()
        }
    }

    fun removePlayer(uuid: UUID){
        queuedConnections.forEach { (key, value) ->
            value.remove(uuid);
        }
    }
}
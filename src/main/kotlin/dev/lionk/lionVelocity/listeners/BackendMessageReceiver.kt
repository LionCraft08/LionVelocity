package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.playerManagement.PlayerData
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import java.util.UUID

object BackendMessageReceiver {
    fun onReceive(server: RegisteredServer, to: TransferrableObject){
        when (to.objectType){
            "lionapi_msg" ->{
                MessageSender.sendPlayerMSG(to)
            }
            "lionapi_request_player_data" -> {
                val uuid = UUID.fromString(to.data.get("uuid"))
                BackendServerManager.getConnection(server)!!.sendMessage(
                    TransferrableObject("lionapi_playerdata")
                        .addValue("uuid", uuid.toString())
                        .addValue("data", PlayerDataManager.getPlayerData(uuid).toString()))
            }
            "lionapi_playerdata" -> {
                PlayerDataManager.setPlayerData(
                    UUID.fromString(
                    to.data.getValue("uuid")),
                    PlayerData.fromString(to.data.getValue("data")))
            }
        }
    }
}
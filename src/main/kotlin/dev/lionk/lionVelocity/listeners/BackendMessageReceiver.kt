package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import de.lioncraft.lionapi.velocity.data.PlayerConfiguration
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.backend.PingedServerStorage
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.playerManagement.PlayerConfigCache
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import dev.lionk.lionVelocity.playerManagement.saveQueueReconnect
import dev.lionk.lionVelocity.utils.toComponent
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object BackendMessageReceiver {
    fun onReceive(server: RegisteredServer, to: TransferrableObject){
        when (to.objectType){
            "lionapi_msg" ->{
                MessageSender.sendPlayerMSG(to)
            }
            "lionapi_request_player_data" -> {
                val uuid = UUID.fromString(to.data.get("uuid"))
                LionVelocity.instance.async {
                    PlayerDataManager.sendPlayerData(server, PlayerConfigCache.getOrCreatePlayerConfig(uuid).get())
                }
            }
            "lionapi_playerdata" -> {
                PlayerConfigCache.saveUpdateCachedPlayer(PlayerConfiguration.fromJson(to.data.getValue("data")))
            }
            "LionLobby_PlayerTransfer" -> {
                val rs = LionVelocity.instance.server.getServer(to.getString("server"))
                val player = LionVelocity.instance.server.getPlayer(UUID.fromString(to.getString("player")))

                if (player.isPresent){
                    if (rs.isPresent){
                        player.get().saveQueueReconnect(rs.get())
                    } else {
                        LionVelocity.instance.logger.error("Server " + to.getString("server") + " wasn't found")
                        LionChat.sendMessageOnChannel("velocity", "<#FF7700>Couldn't find this Server!".toComponent(), player.get())
                    }
                } else LionVelocity.instance.logger.error("Player "+to.getString("player")+ " wasn't found")
            }
            "LionLobby_RequestServerStates" -> {
                val requestedServer = to.getString("server")
                val requestingServer = BackendServerManager.getConnection(server)?:error("Couldn't get the requesting Server!")
                if (requestedServer == null){
                    PingedServerStorage.registerServerStateReceiver(requestingServer)
                    PingedServerStorage.sendServerStatesToReceiver(requestingServer)
                }else{
                    PingedServerStorage.sendServerStateToReceiver(requestingServer,
                        PingedServerStorage.getServerState(
                            LionVelocity.instance.server.getServer(requestedServer).getOrNull()?:error("Couldn't find the requested server: $requestedServer")
                        )!!
                    )
                }
            }
        }
    }
}
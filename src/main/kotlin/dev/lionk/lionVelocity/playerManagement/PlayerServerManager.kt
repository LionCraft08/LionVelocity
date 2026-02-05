package dev.lionk.lionVelocity.playerManagement

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.data.Config
import dev.lionk.lionVelocity.listeners.PlayerPMHandler
import dev.lionk.lionVelocity.utils.toComponent
import kotlin.jvm.optionals.getOrNull

object PlayerServerManager {
    fun queueReconnect(p: Player, server: RegisteredServer){
        PlayerPMHandler.sendMessage(p, "velocity_start_fade:1000")
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance,
            Runnable {
                if (p.isActive) p.createConnectionRequest(server).connectWithIndication()
            }).delay(java.time.Duration.ofSeconds(1)).schedule()
    }

    fun saveQueueReconnect(player: Player, rs: RegisteredServer){
        if(!saveQueueReconnectSilent(player, rs)){
            LionChat.sendMessageOnChannel(
                "velocity",
                "<#FF7700>You are already connected to this Server!".toComponent(),
                player
            )
        }
   }

    fun saveQueueReconnectSilent(player: Player, rs: RegisteredServer):Boolean {
        if(rs != player.currentServer.getOrNull()?.server){
            if (Config.getValue("allowUnknownServerConnections")!!.asBoolean || BackendServerManager.getConnection(rs) != null) {
                player.queueReconnect(rs)
                return true
            }
        }
        return false
    }

}

fun Player.queueReconnect(server: RegisteredServer){
    PlayerServerManager.queueReconnect(this, server)
}

fun Player.saveQueueReconnect(rs: RegisteredServer){
    PlayerServerManager.saveQueueReconnect(this, rs)
}
fun Player.saveQueueReconnectSilent(rs: RegisteredServer):Boolean{
    return PlayerServerManager.saveQueueReconnectSilent(this, rs)
}
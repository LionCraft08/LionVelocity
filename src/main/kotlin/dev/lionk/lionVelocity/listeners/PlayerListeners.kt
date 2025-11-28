package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.data.Config
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import dev.lionk.lionVelocity.playerManagement.WhitelistManagement
import dev.lionk.lionVelocity.utils.toComponent

class PlayerListeners {
    @Subscribe
    fun onJoin(e: ServerPostConnectEvent) {
        val p: Player = e.getPlayer()
        MessageSender.sendHeader(p)
        MessageSender.sendFooter(p)
        LionVelocity.instance.server.getScheduler().buildTask(LionVelocity.instance, Runnable({
            MessageSender.sendFooter(p)
            MessageSender.sendHeader(p)
        })).delay(2, java.util.concurrent.TimeUnit.SECONDS).schedule()
    }

    @Subscribe
    fun onDC(e: DisconnectEvent) {
        for (p in  LionVelocity.instance.server.allPlayers) {
            if (p !== e.player) MessageSender.sendFooter(p)
        }
        PlayerDataManager.getPlayerData(e.player.uniqueId).lastOnline = System.currentTimeMillis()
        PlayerDataManager.getPlayerData(e.player.uniqueId).currentServer = null
    }

    @Subscribe
    fun onJoin(e: PostLoginEvent) {
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
            PlayerDataManager.getPlayerData(e.getPlayer().getUniqueId())
        }).schedule()

        LionVelocity.instance.server.scheduler.buildTask( LionVelocity.instance, Runnable({
            for (p in  LionVelocity.instance.server.allPlayers) {
                MessageSender.sendFooter(p)
            }
        })).delay(2, java.util.concurrent.TimeUnit.SECONDS).schedule()
    }

    @Subscribe
    fun preLogin(e: PreLoginEvent){
        if (e.uniqueId == null ){
            if( !hasReceivedOfflineModeMessage) {
                LionChat.sendLogMessage("The whitelist / ban System is kinda useless in Offline mode!")
                hasReceivedOfflineModeMessage = true
            }
        }else if (WhitelistManagement.isBanned(e.uniqueId!!)){
            e.result = PreLoginEvent.PreLoginComponentResult.denied(Config.getValue("banMessage").asString.toComponent())
        } else if (WhitelistManagement.enabled && !WhitelistManagement.isWhitelisted(e.uniqueId!!)){
            e.result = PreLoginEvent.PreLoginComponentResult.denied(Config.getValue("whitelistMessage").asString.toComponent())
        }
    }
    var hasReceivedOfflineModeMessage = false
}

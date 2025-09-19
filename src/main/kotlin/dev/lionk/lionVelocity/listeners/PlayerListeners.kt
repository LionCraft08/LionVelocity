package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager

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
        for (p in  LionVelocity.instance.server.getAllPlayers()) {
            if (p !== e.getPlayer()) MessageSender.sendFooter(p)
        }
    }

    @Subscribe
    fun onJoin(e: PostLoginEvent) {
        PlayerDataManager.getPlayerData(e.getPlayer().getUniqueId())

        LionVelocity.instance.server.getScheduler().buildTask( LionVelocity.instance, Runnable({
            for (p in  LionVelocity.instance.server.getAllPlayers()) {
                MessageSender.sendFooter(p)
            }
        })).delay(2, java.util.concurrent.TimeUnit.SECONDS).schedule()
    }
}

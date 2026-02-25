package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.event.EventTask.async
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.data.Config
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.messageHandling.TranslationManager
import dev.lionk.lionVelocity.playerManagement.AutoConnectionManager
import dev.lionk.lionVelocity.playerManagement.PlayerConfigCache
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import dev.lionk.lionVelocity.playerManagement.WhitelistManagement
import dev.lionk.lionVelocity.utils.translate
import dev.lionk.lionVelocity.utils.withSuffix
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.function.Function
import java.util.stream.Stream


class PlayerListeners {
    @Subscribe
    fun onJoin(e: ServerPostConnectEvent) {
        val p: Player = e.player
        MessageSender.sendHeader(p)
        MessageSender.sendFooter(p)
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable{
            MessageSender.sendFooter(p)
            MessageSender.sendHeader(p)
        }).delay(2, java.util.concurrent.TimeUnit.SECONDS).schedule()
    }

    @Subscribe
    fun onDC(e: DisconnectEvent) {
        for (p in  LionVelocity.instance.server.allPlayers) {
            if (p !== e.player) MessageSender.sendFooter(p)
        }
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
            val time = System.currentTimeMillis()
            PlayerConfigCache.getOrCreatePlayerConfig(e.player.uniqueId).get().lastOnline = time
        }).schedule()

    }

    @Subscribe
    fun onJoin(e: PostLoginEvent) {
        LionVelocity.instance.server.scheduler.buildTask( LionVelocity.instance, Runnable{
            for (p in  LionVelocity.instance.server.allPlayers) {
                MessageSender.sendFooter(p)
            }
        }).delay(2, java.util.concurrent.TimeUnit.SECONDS).schedule()
    }

    @Subscribe
    fun onLogin(e: ServerConnectedEvent) {
        LionVelocity.instance.async {
            if(BackendServerManager.getConnection(e.server)?.isConnected()?:false){
                PlayerDataManager.sendPlayerData(e.player, e.server)
            }
        }
    }

    /**
     * Announces the newly joined player
     */
    @Subscribe
    fun onServerPostConnect(event: ServerPostConnectEvent) {
        if(!(Config.getValue("announce-join")?.asBoolean?:false)) return
        if (event.previousServer != null) {
            return
        }
        val player = event.player

        // Get the server the player just joined
        val joinedServer =
            player.getCurrentServer().map<RegisteredServer?>(Function { s: ServerConnection? -> s!!.getServer() })
                .orElse(null)

        if (joinedServer == null) return

        val serverName = joinedServer.getServerInfo().getName()

        // Build the message using Adventure (Kyori)
        val message: Component = "general.joined".translate(player.username, serverName)

        // Broadcast to everyone EXCEPT those on the player's current server

        getNotToSameServerConnectedPlayers(player, serverName).forEach({ p -> p.sendMessage(message) })
    }

    fun getNotToSameServerConnectedPlayers(p: Player, serverName:String): Stream<Player> {
        return LionVelocity.instance.server.getAllPlayers().stream()
            .filter({ p ->
                !p.currentServer
                    .map({ s -> s.serverInfo.name.equals(serverName) })
                    .orElse(false)
            })
    }

    @Subscribe
    fun onJoin(e: LoginEvent) {

    }

    @Subscribe
    fun preLogin(e: PreLoginEvent){
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
            PlayerConfigCache.getOrCreatePlayerConfig(e.uniqueId?:return@Runnable)
        }).schedule()
        if (e.uniqueId == null){
            if( !hasReceivedOfflineModeMessage) {
                LionChat.sendLogMessage("The whitelist / ban System is kinda useless in Offline mode!")
                hasReceivedOfflineModeMessage = true
            }
        }else if (WhitelistManagement.isBanned(e.uniqueId!!)){

            e.result = PreLoginEvent.PreLoginComponentResult.denied(TranslationManager.getComponent("general.ban").withSuffix())
        } else if (WhitelistManagement.enabled && !WhitelistManagement.isWhitelisted(e.uniqueId!!)){
            e.result = PreLoginEvent.PreLoginComponentResult.denied(TranslationManager.getComponent("general.not_whitelisted").withSuffix())
        }
    }
    var hasReceivedOfflineModeMessage = false

    @Subscribe
    fun onServerChange(e: ServerConnectedEvent){
        async{
            AutoConnectionManager.removePlayer(e.player.uniqueId)
        }
    }
}

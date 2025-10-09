package dev.lionk.lionVelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import de.lioncraft.lionapi.messages.DM
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.listeners.PlayerPMHandler
import dev.lionk.lionVelocity.playerManagement.saveQueueReconnect
import dev.lionk.lionVelocity.utils.toComponent
import kotlin.time.Duration

class LobbyCommand : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        if (invocation.source() is Player) {
            val p = invocation.source() as Player
            var lobby = "lobby"
            if (invocation.arguments().size >= 1) {
                try {
                    val i = invocation.arguments()[0].toInt()
                    lobby += i
                } catch (e: java.lang.NumberFormatException) {
                }
            }
            val server: java.util.Optional<RegisteredServer?> =  LionVelocity.instance.server.getServer(lobby)
            if (server.isPresent) {
                p.saveQueueReconnect(server.get())
            } else {
                LionVelocity.instance.logger.error("Server wasn't found")
                LionChat.sendMessageOnChannel("velocity", "<#FF7700>Couldn't find this Server!".toComponent(), p)
            }
        } else LionChat.sendMessageOnChannel("velocity", "<#FF7700>You are not a Player".toComponent(), invocation.source())
    }

    override fun suggest(invocation: SimpleCommand.Invocation?): kotlin.collections.MutableList<kotlin.String?>? {
        return super.suggest(invocation)
    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): java.util.concurrent.CompletableFuture<kotlin.collections.MutableList<kotlin.String?>?> {
        val list: kotlin.collections.MutableList<kotlin.String?> = java.util.ArrayList<kotlin.String?>()
        if (invocation.arguments().size > 1) return java.util.concurrent.CompletableFuture.completedFuture<kotlin.collections.MutableList<kotlin.String?>?>(
            list
        )
        list.add("main")
        for (rs in  LionVelocity.instance.server.getAllServers()) {
            var s: kotlin.String = rs.getServerInfo().getName()
            if (s.startsWith("lobby")) {
                s = s.replaceFirst("lobby".toRegex(), "")
                if (!s.isBlank()) list.add(s)
            }
        }
        return java.util.concurrent.CompletableFuture.completedFuture<kotlin.collections.MutableList<kotlin.String?>?>(
            list
        )
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation?): kotlin.Boolean {
        return super.hasPermission(invocation)
    }
}

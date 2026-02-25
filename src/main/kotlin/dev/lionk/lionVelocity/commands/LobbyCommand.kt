package dev.lionk.lionVelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.playerManagement.saveQueueReconnect
import dev.lionk.lionVelocity.utils.toComponent
import net.kyori.adventure.text.Component

class LobbyCommand : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        if (invocation.source() is Player) {
            val p = invocation.source() as Player
            var lobby = "lobby"
            if (invocation.arguments().size >= 1) {
                try {
                    val i = invocation.arguments()[0].toInt()
                    lobby += i
                } catch (ignored: java.lang.NumberFormatException) {}
            }
            val server: java.util.Optional<RegisteredServer?> =  LionVelocity.instance.server.getServer(lobby)
            if (server.isPresent) {
                p.saveQueueReconnect(server.get())
            } else {
                LionVelocity.instance.logger.error("Server $lobby wasn't found")
                LionChat.sendMessageOnChannel("velocity", Component.translatable("general.no_server"), p)
            }
        } else LionChat.sendMessageOnChannel("velocity", Component.translatable("general.not_a_player"), invocation.source())
    }

    override fun suggest(invocation: SimpleCommand.Invocation?): MutableList<String?>? {
        return super.suggest(invocation)
    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): java.util.concurrent.CompletableFuture<MutableList<String?>?> {
        val list: MutableList<String?> = java.util.ArrayList<String?>()
        if (invocation.arguments().size > 1) return java.util.concurrent.CompletableFuture.completedFuture<MutableList<String?>?>(
            list
        )
        list.add("main")
        for (rs in  LionVelocity.instance.server.getAllServers()) {
            var s: String = rs.getServerInfo().getName()
            if (s.startsWith("lobby")) {
                s = s.replaceFirst("lobby".toRegex(), "")
                if (!s.isBlank()) list.add(s)
            }
        }
        return java.util.concurrent.CompletableFuture.completedFuture<MutableList<String?>?>(
            list
        )
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation?): Boolean {
        return super.hasPermission(invocation)
    }
}

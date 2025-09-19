package dev.lionk.lionVelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messages.DM
import dev.lionk.lionVelocity.LionVelocity

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
                if (p.currentServer.get().server != server.get()) {
                    p.createConnectionRequest(server.get()).connectWithIndication()
                } else p.sendMessage(DM.info("You are already connected to this Server"))
            } else p.sendMessage(DM.info("This Server does not exist"))
        } else invocation.source().sendMessage(DM.info("You are not a Player"))
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

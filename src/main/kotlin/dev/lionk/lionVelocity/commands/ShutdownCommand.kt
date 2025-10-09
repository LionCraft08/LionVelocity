package dev.lionk.lionVelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import net.kyori.adventure.text.minimessage.MiniMessage

class ShutdownCommand : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation?) {
        val cmdsource = invocation!!.source()
        val source = if (cmdsource is Player) cmdsource.uniqueId.toString()
            else "console"
        LionVelocity.instance.server.allPlayers.forEach { player ->
            MessageSender.sendKickMessage(player,
                MiniMessage.miniMessage().deserialize(
                    "<gradient:#FF8C00:#AA00AA>This Server Network is shutting down..." +
                            "<reset><br><br><white>Please try again later"
                )
            )
        }

        BackendServerManager.getConnections().forEach { connection ->
            connection.sendMessage(TransferrableObject("lionapi_shutdown")
                .addValue("source", source))
        }

        LionVelocity.instance.server.shutdown()
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        val source = invocation.source()
        if (source is Player){
            return PlayerDataManager.getPlayerData(source.uniqueId)!!.isOP
        }

        return source is ConsoleCommandSource
    }
}
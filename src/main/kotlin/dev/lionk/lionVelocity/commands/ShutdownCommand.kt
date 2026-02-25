package dev.lionk.lionVelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.playerManagement.PlayerConfigCache
import dev.lionk.lionVelocity.utils.translate
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.concurrent.TimeUnit

class ShutdownCommand : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation?) {
        val cmdsource = invocation!!.source()
        val source = if (cmdsource is Player) cmdsource.uniqueId.toString()
            else "console"
        LionVelocity.instance.server.allPlayers.forEach { player ->
            MessageSender.sendKickMessage(player,
                "features.shutdown.kick".translate()
            )
        }


        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable {
            BackendServerManager.getConnections().forEach { connection ->
                connection.sendMessage(TransferrableObject("lionapi_shutdown")
                    .addValue("source", source)
                )
            }

            LionVelocity.instance.server.shutdown()
        }).delay(1, TimeUnit.SECONDS).schedule()


    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        val source = invocation.source()
        if (source is Player){
            return PlayerConfigCache.getCachedPlayerConfig(source.uniqueId)?.isOperator ?: false
        }

        return source is ConsoleCommandSource
    }
}
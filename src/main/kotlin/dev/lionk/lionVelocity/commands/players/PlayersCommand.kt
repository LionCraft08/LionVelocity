package dev.lionk.lionVelocity.commands.players

import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import dev.lionk.lionVelocity.playerManagement.PlayerConfigCache

object PlayersCommand {
    private val node = BrigadierCommand(BrigadierCommand.literalArgumentBuilder("players")
        .requires { if (it is ConsoleCommandSource) true else PlayerConfigCache.getCachedPlayerConfig((it as Player).uniqueId)?.isOperator ?: false }
        .then(
            BrigadierCommand.literalArgumentBuilder("send")
                .redirect(SendCommand.getCommandNode())
        )
        .build())

    fun getCommand() : BrigadierCommand {
        return node
    }
}
package dev.lionk.lionVelocity.commands.players

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.playerManagement.saveQueueReconnectSilent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.jvm.optionals.getOrNull

object SendCommand {
    val command: LiteralCommandNode<CommandSource?> = BrigadierCommand.literalArgumentBuilder("send")
        .then(BrigadierCommand.requiredArgumentBuilder<String>("players", StringArgumentType.string())
            .suggests { _, builder ->
                if(LionVelocity.instance.server.allPlayers.size < 50) {
                    LionVelocity.instance.server.allPlayers.forEach {
                        builder.suggest(it.username)
                    }
                }
                if(LionVelocity.instance.server.allServers.size < 30) {
                    LionVelocity.instance.server.allServers.forEach {
                        builder.suggest(it.serverInfo.name)
                    }
                }
                builder.suggest("everyone")
                return@suggests builder.buildFuture()
            }
            .executes { context ->
                val target = context.getArgument("players", String::class.java)
                if(context.source is Player){
                    val server = (context.source as Player).currentServer.getOrNull()
                    if (server != null){
                        val players = getTargets(target, context)

                        var count = 0
                        for (player in players) {
                            if(player.saveQueueReconnectSilent(server.server)) count++
                        }

                        LionChat.sendMessageOnChannel(
                            "velocity",
                            Component.text("Sent $count players to ${server.serverInfo.name}",
                                TextColor.color(0, 120, 255)),
                            context.source
                        )

                        return@executes Command.SINGLE_SUCCESS
                    }
                }

                LionChat.sendMessageOnChannel(
                    "velocity",
                    Component.text("This command can only be executed as a connected Player. " +
                            "Try specifying a Server",
                    TextColor.color(255, 60, 0)),
                    context.source
                )

                return@executes Command.SINGLE_SUCCESS
            }
            .then(BrigadierCommand.requiredArgumentBuilder<String>("server", StringArgumentType.string())
                .suggests { _, builder ->
                    LionVelocity.instance.server.allServers.forEach {
                        builder.suggest(it.serverInfo.name)
                    }
                    return@suggests builder.buildFuture()
                }
                .executes { context ->
                    val target = context.getArgument("players", String::class.java)
                    val targetServer = context.getArgument("server", String::class.java)

                    val server = getServer(targetServer)
                    if (server != null){
                        val players = getTargets(target, context)
                        var count = 0
                        for (player in players) {
                            if(player.saveQueueReconnectSilent(server)) count++
                        }

                        LionChat.sendMessageOnChannel(
                            "velocity",
                            Component.text("Sent $count players to ${server.serverInfo.name}",
                                TextColor.color(0, 120, 255)),
                            context.source
                        )

                        return@executes Command.SINGLE_SUCCESS
                    } else
                        LionChat.sendMessageOnChannel(
                            "velocity",
                            Component.text("This Server does not exist. ",
                            TextColor.color(255, 60, 0)),
                            context.source

                        )

                    return@executes Command.SINGLE_SUCCESS
                }
            )
        )
        .build()

    fun getCommand(): BrigadierCommand{
        return BrigadierCommand(getCommandNode())
    }

    fun getTargets(target: String, context: CommandContext<CommandSource>): ArrayList<Player>{
        val players = arrayListOf<Player>()
        if (target.startsWith("player:")){
            if (getPlayer(target.substringAfter("player:")) != null) {
                players.add(getPlayer(target.substringAfter("player:"))!!)
            }
        }else {
            if (target.startsWith("server:")) {
                if (getServer(target.substringAfter("server:")) != null) {
                    players.addAll(getServer(target.substringAfter("server:"))!!.playersConnected)
                }
            } else {
                if (getPlayer(target) != null)
                    players.add(getPlayer(target)!!)
                else if (getServer(target) != null)
                    players.addAll(getServer(target)!!.playersConnected)
                else {
                    when (target) {
                        "everyone" -> players.addAll(LionVelocity.instance.server.allPlayers)
                        "self" -> {
                            if (context.source is Player) {
                                players.add(context.source as Player)
                            }
                        }
                    }
                }
            }
        }

        return players
    }

    fun getCommandNode(): LiteralCommandNode<CommandSource?> {
        return command
    }

    fun getPlayer(name: String): Player?{
        return LionVelocity.instance.server.getPlayer(name).getOrNull()
    }
    fun getServer(name: String): RegisteredServer?{
        return LionVelocity.instance.server.getServer(name).getOrNull()
    }
}
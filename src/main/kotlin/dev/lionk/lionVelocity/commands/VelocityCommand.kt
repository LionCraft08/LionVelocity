package dev.lionk.lionVelocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.playerManagement.PlayerConfigCache
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import dev.lionk.lionVelocity.playerManagement.WhitelistManagement
import dev.lionk.lionVelocity.playerManagement.mojang.PlayerCache
import dev.lionk.lionVelocity.utils.toComponent
import net.kyori.adventure.audience.Audience
import java.time.Duration
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.jvm.optionals.getOrNull


object VelocityCommand {
    fun createBrigadierCommand(): BrigadierCommand{
        val node = BrigadierCommand.literalArgumentBuilder("lionvelocity")
            .requires { if (it is ConsoleCommandSource) true else PlayerConfigCache.getCachedPlayerConfig((it as Player).uniqueId)?.isOperator
                ?: false }
            .executes {
                sendVelocityInformation(it.source)
                return@executes Command.SINGLE_SUCCESS
            }
            .then(BrigadierCommand.literalArgumentBuilder("op")
                .executes { context ->
                    var b = true
                    LionChat.sendMessageOnChannel("velocity", "<#00FFFF>List of OPs: ".toComponent(), context.source)
                    PlayerDataManager.playerData!!.forEach { t, u ->
                        if (u.isOP) {
                            b = false
                            LionChat.sendMessageOnChannel("velocity", PlayerCache.getName(u.uuid)?.toComponent()?:u.uuid.toString().toComponent(), context.source)
                        }
                    }
                    if (b) LionChat.sendMessageOnChannel("velocity", "-- Nothing here :/ ".toComponent(), context.source)
                    return@executes Command.SINGLE_SUCCESS
                }
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                    .suggests { context, builder ->
                        LionVelocity.instance.server.allPlayers.forEach { player ->
                            builder.suggest(player.username)
                        }
                        return@suggests builder.buildFuture()
                    }
                    .executes { context ->
                        executeAsync({
                            val player = (context.getArgument<String>("player", String::class.java))
                            val playerID = getPlayer(player)
                            if (playerID != null) {
                                val cachedPlayer = PlayerConfigCache.getOrCreatePlayerConfig((playerID)).get()
                                if(cachedPlayer.isOperator) {
                                    LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "<red>$player is already a global operator".toComponent(),
                                        context.source
                                    )
                                } else {
                                    cachedPlayer.isOperator = true
                                    PlayerDataManager.sendPlayerDataUpdate(cachedPlayer, "isOperator", true)
                                    LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "<green>Made $player a global Operator".toComponent(),
                                        context.source
                                    )
                                }
                            }
                            else LionChat.sendMessageOnChannel("velocity", "<red>Couldn't find this Player".toComponent(), context.source)
                        })
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(BrigadierCommand.literalArgumentBuilder("deop")
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                    .suggests { context, builder ->
                        return@suggests builder.buildFuture()
                    }
                    .executes { context ->
                        executeAsync({
                            val playerName = (context.getArgument<String>("player", String::class.java))
                            val player = getPlayer(playerName)
                            if (player != null) {
                                val cachedPlayer = PlayerConfigCache.getOrCreatePlayerConfig((player)).get()
                                if(cachedPlayer.isOperator) {
                                    cachedPlayer.isOperator = false
                                    PlayerDataManager.sendPlayerDataUpdate(cachedPlayer, "isOperator", false)
                                    LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "<green>Removed Operator Status from $playerName".toComponent(),
                                        context.source
                                    )
                                }else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "<red>$playerName has no operator permissions".toComponent(),
                                    context.source
                                )

                            } else LionChat.sendMessageOnChannel(
                                "velocity",
                                "<red>Couldn't find this Player".toComponent(),
                                context.source
                            )
                        })
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(BrigadierCommand.literalArgumentBuilder("whitelist")
                .then(BrigadierCommand.literalArgumentBuilder("list")
                    .executes { context ->
                        executeAsync({
                        var s = ""
                        WhitelistManagement.players.forEach { player->
                            val name = PlayerCache.getName(player)?:player.toString()
                            s += ("$name, ")
                        }
                        LionChat.sendMessageOnChannel("velocity",
                            "Whitelist is currently ${if (WhitelistManagement.enabled) "<green>enabled" else "<red>disabled"}".toComponent(),
                            context.source)
                        LionChat.sendMessageOnChannel("velocity", s.toComponent(), context.source)

                        })
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(BrigadierCommand.literalArgumentBuilder("add")
                    .then(BrigadierCommand.requiredArgumentBuilder<String>("player", StringArgumentType.word())
                        .executes {context ->
                            executeAsync {
                                val playerName = (context.getArgument<String>("player", String::class.java))
                                val player = getPlayer(playerName)
                                if (player != null)
                                    if (!WhitelistManagement.isWhitelisted(player)) {
                                        WhitelistManagement.whitelist(player)
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "<green>Added $playerName to the whitelist".toComponent(),
                                            context.source
                                        )
                                    } else
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "<red>This Player is already whitelisted".toComponent(),
                                            context.source
                                        )
                                else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "<red>Couldn't find that player".toComponent(),
                                    context.source
                                )
                            }
                            Command.SINGLE_SUCCESS
                        }
                    )
                )
                .then(BrigadierCommand.literalArgumentBuilder("remove")
                    .then(BrigadierCommand.requiredArgumentBuilder<String>("player", StringArgumentType.word())
                        .executes {context ->
                            executeAsync {
                                val playerName = (context.getArgument<String>("player", String::class.java))
                                val player = getPlayer(playerName)
                                if (player != null)
                                    if (WhitelistManagement.isWhitelisted(player)) {
                                        WhitelistManagement.removeFromWhitelist(player)
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "<green>Removed ${playerName} from the whitelist".toComponent(),
                                            context.source
                                        )
                                    } else
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "<red>This Player is not whitelisted".toComponent(),
                                            context.source
                                        )
                                else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "<red>Couldn't find that player".toComponent(),
                                    context.source
                                )
                            }
                            Command.SINGLE_SUCCESS
                        }
                    )
                )
                .then(BrigadierCommand.literalArgumentBuilder("enable")
                    .executes { context ->
                        if (WhitelistManagement.enabled) {
                            LionChat.sendMessageOnChannel("velocity", "<red>The Whitelist is already active".toComponent(), context.source)
                            return@executes 0
                        }
                        WhitelistManagement.enabled = true
                        LionChat.sendMessageOnChannel("velocity", "<green>Enabled the Whitelist successful".toComponent(), context.source)
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
                .then(BrigadierCommand.literalArgumentBuilder("disable")
                    .executes { context ->
                        if (!WhitelistManagement.enabled) {
                            LionChat.sendMessageOnChannel("velocity", "<red>The Whitelist is already disabled".toComponent(), context.source)
                            return@executes 0
                        }
                        WhitelistManagement.enabled = false
                        LionChat.sendMessageOnChannel("velocity", "<green>Disabled the Whitelist successful".toComponent(), context.source)
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(BrigadierCommand.literalArgumentBuilder("ban")
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                    .suggests { context, builder ->
                        LionVelocity.instance.server.allPlayers.forEach { player ->
                            if (!WhitelistManagement.isBanned(player.uniqueId))
                                builder.suggest(player.username)
                        }
                        return@suggests builder.buildFuture()
                    }
                    .executes { context ->
                        executeAsync {
                            val playerName = (context.getArgument<String>("player", String::class.java))
                            val player = getPlayer(playerName)
                            if (player != null && !WhitelistManagement.isBanned(player)) {
                                WhitelistManagement.ban(player, null)
                                LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "<red>Banned ${playerName}".toComponent(),
                                    context.source
                                )
                            } else LionChat.sendMessageOnChannel(
                                "velocity",
                                "<red>Couldn't find this Player".toComponent(),
                                context.source
                            )
                        }
                        return@executes Command.SINGLE_SUCCESS
                    }
                    .then(BrigadierCommand.requiredArgumentBuilder("duration", StringArgumentType.greedyString())
                        .executes { context ->
                            executeAsync {
                                val playerName = (context.getArgument<String>("player", String::class.java))
                                val player = getPlayer(playerName)
                                val duration = convertToDuration(context.getArgument("duration", String::class.java))
                                if (player != null && !WhitelistManagement.isBanned(player)) {
                                    WhitelistManagement.ban(player, duration.toMillis())
                                    LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "<red>Banned ${playerName} for ${duration.toHours()} hours".toComponent(),
                                        context.source
                                    )
                                } else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "<red>Couldn't find this Player".toComponent(),
                                    context.source
                                )
                            }
                            return@executes Command.SINGLE_SUCCESS
                        })
                )
            )
            .then(BrigadierCommand.literalArgumentBuilder("unban")
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                .suggests { context, builder ->
                    LionVelocity.instance.server.allPlayers.forEach { player ->
                        if (WhitelistManagement.isBanned(player.uniqueId))
                            builder.suggest(player.username)
                    }
                    return@suggests builder.buildFuture()
                }
                .executes { context ->
                    executeAsync {
                        val playerName = (context.getArgument<String>("player", String::class.java))
                        val player = getPlayer(playerName)
                        if (player != null && WhitelistManagement.isBanned(player)) {
                            WhitelistManagement.pardon(player)
                            LionChat.sendMessageOnChannel(
                                "velocity",
                                "<red>Unbanned ${playerName}".toComponent(),
                                context.source
                            )
                        } else LionChat.sendMessageOnChannel(
                            "velocity",
                            "<red>Couldn't find this Player".toComponent(),
                            context.source
                        )
                    }
                    return@executes Command.SINGLE_SUCCESS
                }
                )
            )
            .build()
        return BrigadierCommand(node)
    }

    fun executeAsync(sf: Function0<Unit>){
        LionVelocity.instance.async(sf)
    }

    fun getPlayer(name: String): UUID? {
        val player = LionVelocity.instance.server.getPlayer(name).getOrNull()
        return if (player != null) player.uniqueId
        else PlayerCache.getActualUUID(name).get().getOrNull()
    }

    fun sendVelocityInformation(audience: Audience){
        LionChat.sendMessageOnChannel("velocity", ("Velocity Statusinformationen: " +
                "<br>Spieler: <#FF00FF>${LionVelocity.instance.server.playerCount}" +
                "<reset><br>Server: <#FF00FF>${LionVelocity.instance.server.allServers.size}").toComponent(), audience)
    }

    fun convertToDuration(durationString: String?): Duration {
        if (durationString == null || durationString.trim { it <= ' ' }.isEmpty()) {
            return Duration.ZERO
        }

        var totalDuration: Duration = Duration.ZERO
        // Regex to find numbers followed by 'y', 'd', 'h', or 'm'
        val pattern: Pattern = Pattern.compile("(\\d+)\\s*(y|d|h|m)")
        val matcher: Matcher = pattern.matcher(durationString.lowercase(Locale.getDefault()))

        while (matcher.find()) {
            try {
                val value = matcher.group(1).toLong()
                val unit: String = matcher.group(2)

                when (unit) {
                    "y" -> totalDuration = totalDuration.plusDays(value * 365) // Simplified year conversion
                    "d" -> totalDuration = totalDuration.plusDays(value)
                    "h" -> totalDuration = totalDuration.plusHours(value)
                    "m" -> totalDuration = totalDuration.plusMinutes(value)
                }
            } catch (e: NumberFormatException) {
                // Ignore invalid number formats, continue processing
                System.err.println("Invalid number format detected: " + matcher.group(1))
            }
        }
        return totalDuration
    }
}
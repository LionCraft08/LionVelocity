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
import dev.lionk.lionVelocity.utils.translate
import net.kyori.adventure.audience.Audience
import java.time.Duration
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.jvm.optionals.getOrNull
import kotlin.time.toKotlinDuration


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
                    LionChat.sendMessageOnChannel("velocity", "<#00FFFF>List of OPs: ".toComponent(), context.source)
                    executeAsync {
                        var b = true
                        PlayerConfigCache.getOPPlayers().get().forEach { u ->
                            if(PlayerConfigCache.getCachedPlayerConfig(u)?.isOperator?:true){
                                b = false
                                LionChat.sendMessageOnChannel("velocity", PlayerCache.getActualName(u).get()?.getOrNull()?.toComponent()?:u.toString().toComponent(), context.source)
                            }
                        }
                        if (b) LionChat.sendMessageOnChannel("velocity", "-- Nothing here :/ ".toComponent(), context.source)
                    }
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
                                        "command.op.added.failed".translate(player),
                                        context.source
                                    )
                                } else {
                                    cachedPlayer.isOperator = true
                                    PlayerDataManager.sendPlayerDataUpdate(cachedPlayer, "isOperator", true)
                                    LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "command.op.added".translate(player),
                                        context.source
                                    )
                                }
                            }
                            else LionChat.sendMessageOnChannel("velocity", "general.no_player".translate(), context.source)
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
                                        "command.op.remove".translate(playerName),
                                        context.source
                                    )
                                }else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "command.op.remove.failed".translate(playerName),
                                    context.source
                                )

                            } else LionChat.sendMessageOnChannel(
                                "velocity",
                                "general.no_player".translate(),
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
                            val name = LionVelocity.instance.server.getPlayer(player).getOrNull()?.username?:PlayerCache.getActualName(player).get().getOrNull()?:player.toString()
                            s += ("$name, ")
                        }
                        s = s.substring(0, s.length-2)
                            LionChat.sendMessageOnChannel("velocity",
                                (if (WhitelistManagement.enabled)
                                    "command.whitelist.status.enabled"
                                else "command.whitelist.status.disabled").translate(),
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
                                            "command.whitelist.added".translate(playerName),
                                            context.source
                                        )
                                    } else
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "command.whitelist.added.failed".translate(playerName),
                                            context.source
                                        )
                                else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "general.no_player".translate(),
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
                                            "command.whitelist.remove".translate(playerName),
                                            context.source
                                        )
                                    } else
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "command.whitelist.remove.failed".translate(playerName),
                                            context.source
                                        )
                                else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "general.no_player".translate(),
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
                            LionChat.sendMessageOnChannel("velocity", "command.whitelist.enable.failed".translate(), context.source)
                            return@executes 0
                        }
                        WhitelistManagement.enabled = true
                        LionChat.sendMessageOnChannel("velocity", "command.whitelist.enable".translate(), context.source)
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
                .then(BrigadierCommand.literalArgumentBuilder("disable")
                    .executes { context ->
                        if (!WhitelistManagement.enabled) {
                            LionChat.sendMessageOnChannel("velocity", "command.whitelist.disable.failed".translate(), context.source)
                            return@executes 0
                        }
                        WhitelistManagement.enabled = false
                        LionChat.sendMessageOnChannel("velocity", "command.whitelist.disable".translate(), context.source)
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
                            if (player != null) {
                                if (!WhitelistManagement.isBanned(player)) {
                                    WhitelistManagement.ban(player, null)
                                    LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "command.ban.added".translate(playerName),
                                        context.source
                                    )
                                } else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "command.ban.added.failed".translate(playerName),
                                    context.source
                                )
                            } else LionChat.sendMessageOnChannel(
                                "velocity",
                                "general.no_player".translate(),
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
                                if (player != null) {
                                    if (!WhitelistManagement.isBanned(player)) {
                                        WhitelistManagement.ban(player, duration.toMillis())
                                        LionChat.sendMessageOnChannel(
                                            "velocity",
                                            "command.ban.added_duration".translate(playerName, duration.toKotlinDuration().toString()),
                                            context.source
                                        )
                                    } else LionChat.sendMessageOnChannel(
                                        "velocity",
                                        "command.ban.added.failed".translate(playerName),
                                        context.source
                                    )
                                } else LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "general.no_player".translate(),
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
                        if (player != null) {
                            if (WhitelistManagement.isBanned(player)) {
                                WhitelistManagement.pardon(player)
                                LionChat.sendMessageOnChannel(
                                    "velocity",
                                    "command.ban.remove".translate(playerName),
                                    context.source
                                )
                            } else LionChat.sendMessageOnChannel(
                                "velocity",
                                "command.ban.remove.failed".translate(playerName),
                                context.source
                            )
                        } else LionChat.sendMessageOnChannel(
                            "velocity",
                            "general.no_player".translate(),
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
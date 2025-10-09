package dev.lionk.lionVelocity;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import de.lioncraft.lionapi.messageHandling.lionchat.ChannelConfiguration
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import de.lioncraft.lionapi.messages.ColorGradient
import dev.lionk.lionVelocity.backend.BackendServerManager
import dev.lionk.lionVelocity.backend.PingedServerStorage
import dev.lionk.lionVelocity.backend.TCPConnectionWaiter
import dev.lionk.lionVelocity.commands.LobbyCommand
import dev.lionk.lionVelocity.commands.ShutdownCommand
import dev.lionk.lionVelocity.commands.VelocityCommand
import dev.lionk.lionVelocity.data.Config
import dev.lionk.lionVelocity.data.ItemStackManager
import dev.lionk.lionVelocity.listeners.MOTDListener
import dev.lionk.lionVelocity.listeners.PlayerListeners
import dev.lionk.lionVelocity.listeners.PlayerPMHandler
import dev.lionk.lionVelocity.playerManagement.PlayerDataManager
import dev.lionk.lionVelocity.playerManagement.WhitelistManagement
import dev.lionk.lionVelocity.utils.GUIElementRenderer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

@Plugin(
    id = "lionvelocity",
    name = "LionVelocity",
    version = BuildConstants.VERSION,
    description = "Some additional displays that can be used by the Server",
    url = "lionk.dev",
    authors = ["LionCraft"]
)
class LionVelocity @Inject constructor(val  server: ProxyServer, val logger: Logger, @param:DataDirectory val dataDirectory: java.nio.file.Path) {
    init {
        instance = this
        dataDirectory.toFile().mkdirs()

        saveResourceIfNotExists(
            "/config.json",
            Paths.get(dataDirectory.toString(), "config.json")
        )
        saveResourceIfNotExists(
            "/playerdata.json",
            Paths.get(dataDirectory.toString(), "playerdata.json")
        )
        saveResourceIfNotExists(
            "/whitelist.json",
            Paths.get(dataDirectory.toString(), "whitelist.json")
        )
        saveResourceIfNotExists(
            "/servericons/example-server.yml",
            Paths.get(dataDirectory.toString(), "servericons/example-server.yml")
        )

        logger.info("Initialized LionVelocity Plugin ")
    }


    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        registerLionChatChannels()
        Config.loadConfig()

        PlayerDataManager.init()
        WhitelistManagement.loadData()

        server.eventManager.register(this, MOTDListener())
        server.eventManager.register(this, PlayerListeners())
        server.eventManager.register(this, PlayerPMHandler)

        server.channelRegistrar.register(PlayerPMHandler.IDENTIFIER)

        registerCommands()

        queueTimeUpdater()

        TCPConnectionWaiter.init()

        ItemStackManager.load()

        PingedServerStorage.scheduleServerPingInstance()
    }

    @Subscribe
    fun onShutdown(e: ProxyShutdownEvent?) {
        saveData()
    }

    private fun saveData(){
        Config.saveConfig()
        PlayerDataManager.save()
        WhitelistManagement.saveData()
    }

    private fun registerLionChatChannels() {
        LionChat.registerChannel(
            "system", ChannelConfiguration(
                false,
                TextColor.color(100, 200, 200),
                ColorGradient.getNewGradiant(
                    "LionSystems",
                    TextColor.color(250, 0, 250),
                    TextColor.color(100, 50, 255)
                ),
                true
            )
        )
        LionChat.registerChannel(
            "debug", ChannelConfiguration(
                true,
                TextColor.color(100, 100, 100),
                Component.text("DEBUG", NamedTextColor.DARK_BLUE),
                false
            )
        )
        LionChat.registerChannel(
            "msg", ChannelConfiguration(
                false, TextColor.color(150, 150, 255),
                Component.text("MSG", TextColor.color(60, 60, 255)),
                true
            )
        )
        LionChat.registerChannel(
            "teammsg", ChannelConfiguration(
                false, TextColor.color(180, 255, 180),
                Component.text("TeamMSG", TextColor.color(60, 255, 60)),
                true
            )
        )
        LionChat.registerChannel(
            "log", ChannelConfiguration(
                true, TextColor.color(180, 180, 180),
                Component.text("LOG", TextColor.color(100, 100, 100)),
                false
            )
        )
        LionChat.registerChannel(
            "velocity", ChannelConfiguration(
                false, TextColor.color(255, 255, 255),
                ColorGradient.getNewGradiant(
                    "ServerManager",
                    TextColor.color(255, 150, 0),
                    TextColor.color(255, 50, 0)),
                false
            )
        )
    }

    private fun registerCommands(){
        val cm = server.commandManager
        cm.register(cm.metaBuilder("lobby").aliases("l").plugin(this).build(), LobbyCommand())
        cm.register(cm.metaBuilder("shutdown").aliases("globalshutdown").plugin(this).build(), ShutdownCommand())
        cm.register(
            cm.metaBuilder("lionvelocity").aliases("lvc").plugin(this).build(), VelocityCommand.createBrigadierCommand()
        )
    }

    private fun queueTimeUpdater(){
        server.getScheduler().buildTask(this, Runnable {
            for (p in server.getAllPlayers()) {
                p.sendPlayerListHeader(GUIElementRenderer.getHeader(TimeZone.getDefault().getID()))
            }
        }).repeat(Duration.ofMinutes(1))
            .delay((60 - Calendar.getInstance().get(Calendar.SECOND)).toLong(), TimeUnit.SECONDS).schedule()

        server.scheduler.buildTask(this, Runnable{
            saveData()
        }).repeat(120, TimeUnit.SECONDS).schedule()
    }

    fun saveResourceIfNotExists(resource: String?, outputPath: Path): Boolean {
        if (resource == null || resource.isEmpty() || !resource.startsWith("/")) {
            System.err.println("Error: Resource path must be a non-empty absolute path starting with '/' (e.g., /com/example/file.txt).")
            return false
        }

        val targetFile = outputPath.toFile()

        if (targetFile.exists()) {
            if (targetFile.isDirectory()) {
                return false
            }
            return false
        }
        try {
            LionVelocity::class.java.getResourceAsStream(resource).use { resourceStream ->
                if (resourceStream == null) {
                    return false
                }
                val parentDir = outputPath.getParent()
                if (parentDir != null) {
                    if (!Files.exists(parentDir)) {
                        Files.createDirectories(parentDir)
                    }
                }

                Files.copy(resourceStream, outputPath)
                return true
            }
        } catch (e: IOException) {
            System.err.println("Error saving resource '" + resource + "' to '" + outputPath + "': " + e.message)
            e.printStackTrace()
            return false
        } catch (e: SecurityException) {
            System.err.println("Error creating directories for '" + outputPath + "' due to security restrictions: " + e.message)
            e.printStackTrace()
            return false
        }
    }


    companion object{
        lateinit var instance: LionVelocity
    }
}


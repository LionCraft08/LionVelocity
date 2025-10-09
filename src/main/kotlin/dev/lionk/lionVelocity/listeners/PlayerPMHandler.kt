package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import org.yaml.snakeyaml.events.Event

object PlayerPMHandler {
    val IDENTIFIER: MinecraftChannelIdentifier = MinecraftChannelIdentifier.create("lionvelocity", "connection")
    @Subscribe
    fun onReceive(event: PluginMessageEvent){
        if (IDENTIFIER != event.identifier) return

    }

    fun sendMessage(player: Player, message: String){
        player.sendPluginMessage(IDENTIFIER, message.toByteArray())
    }
}
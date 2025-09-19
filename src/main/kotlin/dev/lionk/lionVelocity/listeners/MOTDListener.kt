package dev.lionk.lionVelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import dev.lionk.lionVelocity.data.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class MOTDListener {
    @Subscribe
    fun onRequest(e: ProxyPingEvent) {
        e.ping = ServerPing(
            e.ping.version,
            e.ping.players.orElse(null),
            getRandomMotdComponent(),
            e.ping.favicon.orElse(null)
        )
    }

    val motds = Config.getValue("motds").asJsonArray.map { MiniMessage.miniMessage().deserialize(it.asString) }

    fun getRandomMotdComponent(): Component{
        return motds[(Math.random() * motds.size).toInt()]
    }
}
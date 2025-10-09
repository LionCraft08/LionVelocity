package dev.lionk.lionVelocity.backend

import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerPing
import de.lioncraft.lionapi.velocity.data.ServerState
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.data.ItemStackManager
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import kotlin.jvm.optionals.getOrNull

object PingedServerStorage {
    private val servers = HashMap<RegisteredServer, ServerState>()

    fun updateServer(rs: RegisteredServer, newServerState: ServerState){
        if (!servers.contains(rs)) servers.put(rs, newServerState)
        else {
            val oldServerState = servers.get(rs)!!
            if (
                oldServerState.updateEnabled(newServerState.isOnline)
                ||oldServerState.updateFavicon(newServerState.base64Favicon)
                ||oldServerState.updateMOTD(newServerState.motdString)
                ||oldServerState.updateMaxPlayers(newServerState.maxPlayers)
                ||oldServerState.updateCurrentPlayers(newServerState.currentPlayers)
                ) {
                    sendServerStateToReceivers(newServerState)
            }
        }
    }

    fun updateServer(rs: RegisteredServer, serverPing: ServerPing?){
        if (serverPing == null) updateServer(rs, ServerState(
            rs.serverInfo.name,
            false,
            null,
            null,
            ItemStackManager.items.get(rs.serverInfo.name),
            null,
            rs.playersConnected.size
        )) else updateServer(rs, ServerState(
        rs.serverInfo.name,
        true,
            JSONComponentSerializer.json().serialize(serverPing.descriptionComponent),
        serverPing.favicon.getOrNull()?.base64Url,
        ItemStackManager.items.get(rs.serverInfo.name),
        serverPing.players.getOrNull()?.max,
        rs.playersConnected.size
        ))
    }

    fun sendSinglePing(rs: RegisteredServer) {
        rs.ping().orTimeout(3, TimeUnit.SECONDS)
            .handle<Any?>(BiFunction { serverPing: ServerPing?, throwable: Throwable? ->
                updateServer(rs, serverPing)
                null
            })
    }

    private fun sendPings() {
        for (rs in LionVelocity.instance.server.allServers) {
            sendSinglePing(rs)
        }
    }

    fun scheduleServerPingInstance(){
        LionVelocity.instance.server.scheduler.buildTask(LionVelocity.instance, Runnable{
            sendPings()
        }).repeat(30, TimeUnit.SECONDS).schedule()
    }

    fun sendServerStateToReceiver(connection: AbstractConnection, serverState: ServerState){
        connection.sendMessage(
            TransferrableObject("LionLobby_ServerState")
                .addValue("data", serverState.toString())
        )
    }

    fun sendServerStatesToReceiver(connection: AbstractConnection){
        servers.forEach {
            sendServerStateToReceiver(connection, it.value)
        }
    }

    fun sendServerStateToReceivers(serverState: ServerState){
        for (abs in serverStateReceiver){
            if (abs.isConnected()){
                sendServerStateToReceiver(abs, serverState)
            }
        }

        serverStateReceiver.removeIf { it.isEnded() }
    }

    fun getServerState(registeredServer: RegisteredServer): ServerState? {
        return servers.get(registeredServer)
    }

    fun registerServerStateReceiver(abstractConnection: AbstractConnection){
        if (serverStateReceiver.contains(abstractConnection)) return
        serverStateReceiver.add(abstractConnection)
    }

    private val serverStateReceiver = ArrayList<AbstractConnection>()
}
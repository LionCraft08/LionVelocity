package dev.lionk.lionVelocity.backend

import com.velocitypowered.api.proxy.server.RegisteredServer

object BackendServerManager {
    private val connections: HashMap<String, AbstractConnection> = HashMap()

    fun registerNewConnection(ac: AbstractConnection){
        connections[ac.server!!] = ac
    }

    fun getConnection(serverName: String): AbstractConnection?{
        return (connections[serverName])
    }

    fun getConnection(rs: RegisteredServer): AbstractConnection?{
        return getConnection(rs.serverInfo.name)
    }

    fun removeConnection(name: String){
        connections.remove(name)
    }

    fun getConnections(): Collection<AbstractConnection> {
        return connections.values
    }



}
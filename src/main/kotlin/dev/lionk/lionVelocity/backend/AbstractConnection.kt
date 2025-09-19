package dev.lionk.lionVelocity.backend

import com.google.gson.Gson
import com.velocitypowered.api.proxy.server.RegisteredServer
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.listeners.BackendMessageReceiver

abstract class AbstractConnection (
    var server: String?
){

    protected val gson = Gson()

    abstract fun isConnected(): Boolean
    abstract fun sendMessage(message: String)
    abstract fun isEnded(): Boolean
    abstract fun endConnection()

    fun getServer(): RegisteredServer {
        return LionVelocity.instance.server.getServer(server).get()
    }

    fun sendMessage(to: TransferrableObject){
        sendMessage(gson.toJson(to))
    }

    protected fun onMessageReceive(receivedLine: String?) {
        BackendMessageReceiver.onReceive(getServer(), TransferrableObject.getFromJson(receivedLine))
    }


}
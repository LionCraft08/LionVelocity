package dev.lionk.lionVelocity.backend

import com.velocitypowered.api.proxy.server.RegisteredServer
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.data.Config
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object TCPConnectionWaiter {
    private var serverSocket: ServerSocket? = null

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    fun init(){
        executorService.submit {
            serverSocket = ServerSocket(Config.getValue("TCPServerPort").asInt)
            serverSocket!!.setSoTimeout(0)
            scheduleNewWaiter()
        }
    }

    fun scheduleNewWaiter(){
        executorService.submit {
            LionVelocity.instance.logger
                .info("TCP Server started, waiting for client connection...")
            val client = serverSocket!!.accept()
            LionVelocity.instance.logger
                .info("Got a connection from {}", client.port)

            TCPConnection(null, client)

            scheduleNewWaiter()
        }
    }

    fun getServerByIP(inetAddress: InetAddress, port: Int): RegisteredServer? {
        for (r in LionVelocity.instance.server.allServers){
            if (r.serverInfo.address.address == inetAddress
                && r.serverInfo.address.port == port){
                return r
            }
        }
        return null
    }
}
package dev.lionk.lionVelocity.backend

import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

class TCPConnection (
    serverName: String?,
    val clientSocket: Socket
): AbstractConnection(
    serverName
){

    private var isSetUp = false
    private var out: PrintWriter? = null
    private var `in`: BufferedReader? = null
    private val executorService: ExecutorService = Executors.newFixedThreadPool(2)

    init {
        LionVelocity.instance.logger
            .info("Client connected from {}", clientSocket.getInetAddress().hostAddress)

        setupStreams()
        readMessages()
    }

    fun setServer(host: String, port: Int){
        val rs = TCPConnectionWaiter.getServerByIP(InetAddress.getByName(host), port)
        if(rs == null){
            LionVelocity.instance.logger.warn("A Server ({}:{}) that is not registered has connected to the Backend communication service" +
                    "This might be a security risk!", host, port)
            server = UUID.randomUUID().toString()
        }else{
            server = rs.serverInfo.name
            LionVelocity.instance.logger.info("Server {} completely connected!", server)
        }
        isSetUp = true
        BackendServerManager.registerNewConnection(this)

    }

    @Throws(IOException::class)
    private fun setupStreams() {
        out = PrintWriter(clientSocket.getOutputStream(), true)
        `in` = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
    }

    override fun sendMessage(message: String) {
        executorService.submit(Runnable {
            if (out != null) {
                out!!.println(message)
            } else {
                LionVelocity.instance.logger
                    .info("Cannot send message. Connection is not active.")
            }
        })
    }

    /**
     * Starts a new thread to continuously read messages from the input stream.
     * This method is called after a connection is established.
     */
    private fun readMessages() {
        executorService.submit(Runnable {
            var receivedLine: String? = null
            try {
                while (isRunning && (`in`!!.readLine().also { receivedLine = it }) != null) {

                    // Call the callback method on the main plugin class
                    lastAliveCheck = System.currentTimeMillis()
                    if (receivedLine!!.startsWith("setupconnection") && !isSetUp){
                        val values = receivedLine.split(":")
                        setServer(values[1], values[2].toInt())
                    }else onMessageReceive(receivedLine)
                }
            } catch (e: IOException) {
                if (isRunning) { // Only log if not a planned shutdown
                    LionVelocity.instance.logger
                        .info("Connection lost with peer.", e)
                }
            } finally {
                shutdown()
            }
        })
    }

    private var lastAliveCheck: Long = 0

    fun sendAliveCheck() {
        sendMessage(TransferrableObject("lionapi_alive_check"))
        LionVelocity.instance.server.getScheduler().buildTask(LionVelocity.instance, Runnable({
            if (System.currentTimeMillis() - lastAliveCheck > 5000) {
                this.shutdown()
            }
        })).delay(5000, TimeUnit.MILLISECONDS).schedule()
    }

    fun getMillisSinceLastAlive(): Long {
        return System.currentTimeMillis() - lastAliveCheck
    }

    /**
     * Shuts down all connections and the thread pool.
     */
    private fun shutdown() {
        this.isRunning = false
        try {
            if (out != null) out!!.close()
            if (`in` != null) `in`!!.close()
            if (clientSocket != null) clientSocket.close()
        } catch (e: IOException) {
            LionVelocity.instance.logger
                .info("Error while closing network connections.", e)
        } finally {
            if (!executorService.isShutdown) {
                executorService.shutdownNow()
            }
            isCancelled = true
        }
    }



    @Volatile
    private var isRunning = true

    @Volatile
    private var isCancelled = false


    override fun isConnected(): Boolean {
        return isRunning
    }

    override fun isEnded(): Boolean {
        return isCancelled
    }

    override fun endConnection() {
        shutdown()
    }

}
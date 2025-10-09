package dev.lionk.lionVelocity.playerManagement

import com.google.gson.Gson
import com.velocitypowered.api.util.UuidUtils
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.messageHandling.MessageSender
import dev.lionk.lionVelocity.utils.toComponent
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.UUID


object WhitelistManagement {
    val file: File = File(LionVelocity.instance.dataDirectory.toFile(), "whitelist.json")
    var enabled = false
    var players = ArrayList<UUID>()
    var banned = HashMap<UUID, Long?>()
    private var whitelistStorage: WhitelistStorage = WhitelistStorage()

    override fun toString(): String{
        whitelistStorage.enabled = enabled
        whitelistStorage.banned.clear()
        whitelistStorage.players.clear()
        whitelistStorage.players = players.map { uUID -> uUID.toString() } as ArrayList<String>
        whitelistStorage.banned = banned.mapKeys { it.key.toString() } as HashMap<String, Long?>
        return Gson().toJson(whitelistStorage)
    }
    fun loadData(){
        whitelistStorage = Gson().fromJson(FileReader(file), WhitelistStorage::class.java)
        players = whitelistStorage.players.map { str -> UUID.fromString(str) } as ArrayList<UUID>
        banned = whitelistStorage.banned.mapKeys { UUID.fromString(it.key) } as HashMap<UUID, Long?>
        enabled = whitelistStorage.enabled
    }

    fun saveData(){
        try {
            FileWriter(file).use { writer ->
                toString()
                Gson().toJson(whitelistStorage, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun ban(player: UUID, duration: Long?){
        var time: Long? = null
        if (duration != null){
            time = System.currentTimeMillis() + duration
        }
        banned[player] = time
        val playerObj = LionVelocity.instance.server.getPlayer(player)
        if (playerObj.isPresent)
            MessageSender.sendKickMessage(playerObj.get(), "<red>You are banned from this Server!".toComponent())
    }

    fun whitelist(uuid: UUID){
        players.add(uuid)
    }

    fun removeFromWhitelist(uuid: UUID){
        players.remove(uuid)
    }

    fun isBanned(uuid: UUID): Boolean{
        if (banned.containsKey(uuid)){
            if (banned.get(uuid) == null) return true
            else if (banned.get(uuid)!! < System.currentTimeMillis()){
                banned.remove(uuid)
                return false
            }else return true
        }else return false
    }

    fun isWhitelisted(uuid: UUID): Boolean{
        return players.contains(uuid)
    }
}



data class WhitelistStorage
(
    var enabled: Boolean = false,
    var players: ArrayList<String> = ArrayList<String>(),
    var banned: HashMap<String, Long?> = HashMap<String, Long?>()
)

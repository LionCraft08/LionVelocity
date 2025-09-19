package dev.lionk.lionVelocity.playerManagement

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.lionk.lionVelocity.LionVelocity
import java.io.*
import java.util.*

object PlayerDataManager {

    val file: File = File(LionVelocity.instance.dataDirectory.toFile(), "playerdata.json")

    val gson: Gson = Gson()
    
    var playerData: HashMap<UUID, PlayerData>? = null


    fun init() {
        try {
            val typeOfMap =
                TypeToken.getParameterized(HashMap::class.java, UUID::class.java, PlayerData::class.java).type
            playerData = gson.fromJson<HashMap<UUID, PlayerData>?>(FileReader(file), typeOfMap)
            if (playerData == null) playerData = HashMap()
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
    }

    fun save() {
        try {
            FileWriter(file).use { writer ->
                // Convert the HashMap object to a JSON string and write to file
                gson.toJson(playerData, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun getPlayerData(uuid: UUID): PlayerData? {
        if (!playerData!!.containsKey(uuid)) playerData!![uuid] = PlayerData(uuid)
        return playerData!![uuid]
    }

    fun setPlayerData(uuid: UUID, playerData: PlayerData){
        PlayerDataManager.playerData!![uuid] = playerData
    }


}
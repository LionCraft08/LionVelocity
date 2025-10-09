package dev.lionk.lionVelocity.playerManagement

import com.google.common.base.Strings
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.velocitypowered.api.proxy.Player
import dev.lionk.lionVelocity.LionVelocity
import java.util.*

class PlayerData(val uuid: UUID) {
    var name: String? = null
        set(value) {
            if (value != null) PlayerDataManager.playerMapping.put(value, uuid)
        }
    var isOP: Boolean
    var data: HashMap<String, JsonElement> = HashMap()
    var lastOnline: Long = 0

    var currentServer: String? = null
        get() = field
        set(value) {
            field = value

        }

    init {
        val p: Player? = LionVelocity.instance.server.getPlayer(uuid).orElse(null)

        if (p != null) {
            lastOnline = System.currentTimeMillis()
            name = p.username
            if (p.clientBrand != null) data["ClientBrand"] = JsonPrimitive(p.clientBrand)
        }

        isOP = false
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object{
        fun fromString(string: String): PlayerData{
            return Gson().fromJson(string, PlayerData::class.java)
        }
    }
}

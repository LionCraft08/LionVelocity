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
        private set
    var isOP: Boolean
    var locale: Locale? = null
    var data: HashMap<String, JsonElement> = HashMap()

    init {
        val p: Player? = LionVelocity.instance.server.getPlayer(uuid).orElse(null)

        if (p != null) {
            name = p.username
            data["ClientBrand"] = JsonPrimitive(p.clientBrand)
            locale = p.effectiveLocale
        } else {
            locale = null
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

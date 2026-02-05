package dev.lionk.lionVelocity.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.lionk.lionVelocity.LionVelocity
import java.nio.file.Files

object Config {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private lateinit var jo: JsonObject
    fun getJO(): JsonObject = jo

    fun getValue(key: String): JsonElement? {
        return jo.get(key)
    }

    fun loadConfig(){
        val string = String(Files.readAllBytes(LionVelocity.instance.dataDirectory.resolve("config.json")))
        jo = gson.fromJson(string, JsonObject::class.java)
    }

    fun saveConfig(){
        Files.write(
            LionVelocity.instance.dataDirectory.resolve("config.json"),
            gson.toJson(jo)
                .toByteArray()
        )
    }
}
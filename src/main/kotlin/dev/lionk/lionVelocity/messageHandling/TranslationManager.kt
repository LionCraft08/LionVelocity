package dev.lionk.lionVelocity.messageHandling

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.lionk.lionVelocity.LionVelocity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.TranslationStore
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*


object TranslationManager {
    val gson = GsonBuilder().setPrettyPrinting().create()
    init{

    }

    fun save(file: Path){
        if(!file.exists()){
            file.createDirectory()
        }

        val index = LionVelocity::class.java.getResourceAsStream("/lang/index.txt").use { it.bufferedReader().readLines() }
        index.forEach {
            LionVelocity.instance.saveResourceIfNotExists("/lang/${it.trim()}", file.resolve(it.trim()))
        }
    }

    lateinit var store: HashMap<String, String>

    fun load(file: Path){
        val jo = gson.fromJson(file.readText(), JsonObject::class.java)


        val internalJO = gson.fromJson(InputStreamReader(LionVelocity::class.java
            .getResourceAsStream("/lang/${file.name}")!!),
            JsonObject::class.java)
        var hasChanged = false
        internalJO.entrySet().forEach {(key, value) ->
            if (!jo.has(key)){
                jo.add(key, value)
                hasChanged = true
            }
        }
        if (hasChanged) {
            Files.writeString(file, gson.toJson(jo))
        }

        val translations = hashMapOf<String, String>()
        for((key, value) in jo.entrySet()){
            translations[key] = value.asString
        }
        store = translations

        Locale.setDefault(Locale.Category.DISPLAY, Locale.forLanguageTag(file.nameWithoutExtension))
        Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag(file.nameWithoutExtension))
    }

    fun getComponent(key: String): Component{
        return MiniMessage.miniMessage().deserialize(store[key] ?: "'$key' not found.")
    }

    fun getComponent(key: String, vararg components: String): Component{
        val rawMessage: String = store[key] ?: "'$key' not found."

        // 2. Build the resolvers
        val resolvers: MutableList<TagResolver> = ArrayList<TagResolver>()
        var i = 0
        components.forEach({ value ->
            // Use unparsed to prevent "tag injection" from user input
            resolvers.add(Placeholder.unparsed(i.toString(), value))
            i++
        })

        // 3. Deserialize with the resolvers
        return MiniMessage.miniMessage().deserialize(rawMessage, TagResolver.resolver(resolvers))
    }

    fun getComponent(key: String, vararg components: ComponentLike): Component{
        val rawMessage: String = store[key] ?: "'$key' not found."

        // 2. Build the resolvers
        val resolvers: MutableList<TagResolver> = ArrayList<TagResolver>()
        var i = 0
        components.forEach({ value ->
            // Use unparsed to prevent "tag injection" from user input
            resolvers.add(Placeholder.component(i.toString(), value))
            i++
        })

        // 3. Deserialize with the resolvers
        return MiniMessage.miniMessage().deserialize(rawMessage, TagResolver.resolver(resolvers))
    }
}
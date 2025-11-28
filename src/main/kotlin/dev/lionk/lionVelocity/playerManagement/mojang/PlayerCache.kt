package dev.lionk.lionVelocity.playerManagement.mojang

import com.google.common.collect.BiMap
import de.lioncraft.lionapi.mojang.MojangUUIDFetcher
import dev.lionk.lionVelocity.LionVelocity
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture

object PlayerCache {
    private val fetcher = MojangUUIDFetcher()
    private val cache = HashMap<String, UUID>()

    fun getUUIDOrNull(name: String): UUID?{
        return cache.get(name)
    }
    fun hasUUIDCached(name: String): Boolean{
        return cache.get(name) != null
    }
    fun putPlayerUUID(name: String, uuid: UUID){
        cache[name] = uuid
    }

    fun getActualUUID(name: String): CompletableFuture<Optional<UUID>>{
        if (cache.contains(name)){
            return CompletableFuture<Optional<UUID>>().completeAsync {
                Thread.sleep(1)
                return@completeAsync Optional.of<UUID>(cache.get(name)!!)
            }
        }
        else return makeAPICall(name)
    }

    fun getName(uuid: UUID): String?{
        for (name in cache.keys){
            if (cache.get(name)!! == uuid)
                return name
        }
        return null
    }

    private fun makeAPICall(name: String): CompletableFuture<Optional<UUID>>{
        val cf = fetcher.getUUID(name)
        cf.whenComplete { t, u -> if (t.isPresent) putPlayerUUID(name, t.get()) }
        return cf
    }

}
package dev.lionk.lionVelocity.utils

import com.velocitypowered.api.proxy.Player
import dev.lionk.lionVelocity.messageHandling.TranslationManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.Locale

fun String.toComponent(): Component{
    return MiniMessage.miniMessage().deserialize(this)
}
fun String.translate(vararg args: ComponentLike): Component{
    return TranslationManager.getComponent(this, *args)
}
fun String.translate(): Component{
    return TranslationManager.getComponent(this)
}
fun String.translate(vararg args: String): Component{
    return TranslationManager.getComponent(this, *args)
}

fun Component.withSuffix():Component{
    return this.append("general.kick_suffix".translate())
}
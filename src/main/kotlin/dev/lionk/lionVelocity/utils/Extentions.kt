package dev.lionk.lionVelocity.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun String.toComponent(): Component{
    return MiniMessage.miniMessage().deserialize(this)
}
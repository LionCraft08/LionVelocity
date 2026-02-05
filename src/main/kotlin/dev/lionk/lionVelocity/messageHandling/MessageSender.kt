package dev.lionk.lionVelocity.messageHandling

import com.velocitypowered.api.proxy.Player
import de.lioncraft.lionapi.messageHandling.MSG
import de.lioncraft.lionapi.messageHandling.lionchat.LionChat
import de.lioncraft.lionapi.velocity.data.TransferrableObject
import dev.lionk.lionVelocity.LionVelocity
import dev.lionk.lionVelocity.utils.GUIElementRenderer.getFooter
import dev.lionk.lionVelocity.utils.GUIElementRenderer.getHeader
import dev.lionk.lionVelocity.utils.toComponent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import java.util.*

object MessageSender {
    fun sendHeader(a: Audience) {
        a.sendPlayerListHeader(getHeader(TimeZone.getDefault().getID()))
    }

    fun sendFooter(p: Player) {
        p.sendPlayerListFooter(getFooter(p.getCurrentServer().get().getServer().getServerInfo().getName()))
    }

    private val suffix = ("<reset><br><br><br><br><br>" +
            "<gradient:#FF00AA:#00AAFF>______________________<br><reset>"+
            "<gradient:#FF00AA:#00AAFF>Powered by LionSystems").toComponent()
    fun sendKickMessage(p: Player, msg: Component){
        p.disconnect(msg.append(suffix))
    }

    fun sendPlayerMSG(`object`: TransferrableObject){
        val source = JSONComponentSerializer.json().deserialize(`object`.getData().get("source")!!)
        val message = JSONComponentSerializer.json().deserialize(`object`.getData().get("message")!!)
        val target = `object`.data["target"]
        val srcPlayerID = UUID.fromString(`object`.getData().get("sourcePlayer"))
        val p: Optional<Player?> = LionVelocity.instance.server.getPlayer(target)
        val srcPlayer: Optional<Player?> = LionVelocity.instance.server.getPlayer(srcPlayerID)

        if (p.isEmpty()) {
            LionChat.sendMSG(null, MSG.noPlayer.getText(), srcPlayer.get())
        } else {
            LionChat.sendMSG(source, message, p.get())
            LionChat.sendMSG(
                Component.text("Du -> ").append(Component.text(p.get().getUsername())),
                message,
                srcPlayer.get()
            )
        }
    }

}

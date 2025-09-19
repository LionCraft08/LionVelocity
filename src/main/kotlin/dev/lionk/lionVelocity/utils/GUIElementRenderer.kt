package dev.lionk.lionVelocity.utils

import de.lioncraft.lionapi.messages.ColorGradient
import dev.lionk.lionVelocity.LionVelocity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object GUIElementRenderer {
    var line: Component = ColorGradient.getNewGradiant(
    "-----------------------------",
    TextColor.color(250, 0, 255),
    TextColor.color(0, 0, 255)
    )

    var ls: Component = ColorGradient.getNewGradiant(
        "LionSystems Servernetzwerk",
        TextColor.color(0, 100, 255),
        TextColor.color(0, 255, 255)
    )

    fun getHeader(timeZone: String): Component {
        var timeZone = timeZone
        if (timeZone.isBlank()) timeZone = TimeZone.getDefault().getID()
        return Component.text("").appendNewline().append(ls).appendNewline().appendNewline().append(getTime(timeZone))
            .appendNewline().append(line)
    }

    fun getFooter(servername: String?): Component {
        return line.appendNewline().append(Component.text("Server: $servername", TextColor.color(0, 255, 255)))
            .appendNewline().appendNewline()
            .append(Component.text("Netzwerk", TextColor.color(0, 200, 255), TextDecoration.UNDERLINED)).appendNewline()
            .appendNewline()
            .append(
                Component.text(
                    "Player: " + LionVelocity.instance.server.allPlayers.size,
                    TextColor.color(0, 150, 255)
                )
            )
            .append(
                Component.text(
                    "        Server: " +  LionVelocity.instance.server.getAllServers().size,
                    TextColor.color(0, 100, 255)
                )
            ).appendNewline()
    }

    fun getTime(timeZone: String): Component {
        val zonedDateTime = ZonedDateTime.now(ZoneId.of(timeZone))
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy    HH:mm z")
        return ColorGradient.getNewGradiant(
            zonedDateTime.format(formatter),
            TextColor.color(128, 0, 255),
            TextColor.color(255, 0, 128)
        )
    }
}
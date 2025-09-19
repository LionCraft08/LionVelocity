package de.lioncraft.lionapi.messageHandling.lionchat;

import com.velocitypowered.api.proxy.Player;
import de.lioncraft.lionapi.messageHandling.MSG;
import dev.lionk.lionVelocity.LionVelocity;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;

public final class LionChat {
    private static final HashMap<String, ChannelConfiguration> channels = new HashMap<>();
    private LionChat(){}

    public static void sendDebugMessage(String message){
        sendDebugMessage(Component.text(message));
    }
    public static void sendDebugMessage(Component message){
        sendMessageOnChannel("debug", message);
        sendMessageOnChannel("debug", message,  LionVelocity.instance.getServer().getConsoleCommandSource());
    }
    public static void sendLogMessage(String message){
        sendLogMessage(Component.text(message));
    }
    public static void sendLogMessage(Component message){
        sendMessageOnChannel("log", message);
        sendMessageOnChannel("log", message,  LionVelocity.instance.getServer().getConsoleCommandSource());
    }
    public static void sendMessageOnChannel(String channel, Component message){
        ChannelConfiguration c = channels.get(channel);
        c.getPlayersToBroadcastTo().forEach(player -> {
            sendMessageOnChannel(channel, message, player);
        });
    }
    public static void sendMessageOnChannel(String channel, MSG message, Audience target){
        sendMessageOnChannel(channel, message.getText(), target);
    }
    public static void sendSystemMessage(MSG message, Audience target){
        sendMessageOnChannel("system", message, target);
    }
    public static void sendSystemMessage(String message, Audience target){
        sendMessageOnChannel("system", Component.text(message), target);
    }
    public static void sendSystemMessage(Component message, Audience target){
        sendMessageOnChannel("system", message, target);
    }
    public static void sendError(String message, Audience target){
        sendMessageOnChannel("system", Component.text(message, TextColor.color(255, 128, 0)), target);
    }
    public static void sendMSG(@Nullable Component source, Component Message, Audience target){
        sendCustomMSG("msg", source, Message, target);
    }
    public static void sendTeamMSG(@Nullable Component source, Component message, Audience target){
        sendCustomMSG("teammsg", source, message, target);
    }
    private static void sendCustomMSG(String channel, @Nullable Component source, Component Message, Audience target){
        ChannelConfiguration c = channels.get(channel);
        if (source == null) source = Component.text("");
        else source = source.appendSpace();
        if (target instanceof Player p) {
            if (!c.canReceive(p)) return;
        }
        target.sendMessage(
                c.getPrefix().appendSpace()
                        .append(source)
                        .append(Component.text(">>", c.getDefaultColor())).appendSpace()
                        .append(Message.colorIfAbsent(c.getDefaultColor()))

        );
    }
    public static void sendMessageOnChannel(ChannelConfiguration c, Component message, Audience target){
        if (target instanceof Player p) {
            if (!c.canReceive(p)) return;
        }
        //target.sendMessage(getFullPrefix(c.getPrefix(), c.getDefaultColor()).append(message.colorIfAbsent(c.getDefaultColor())));
        target.sendMessage(
                c.getPrefix().appendSpace()
                        .append(Component.text(">>", c.getDefaultColor())).appendSpace()
                        .append(message.colorIfAbsent(c.getDefaultColor()))

        );
    }
    public static void sendMessageOnChannel(String channel, Component message, Audience target){
        if (Objects.equals(channel, "msg")) sendMSG(null, message, target);
        else if (channels.containsKey(channel)){
            ChannelConfiguration c = channels.get(channel);
            sendMessageOnChannel(c, message, target);
        } else {
            target.sendMessage(getFullPrefix(channels.get("system").getPrefix(), channels.get("system").getDefaultColor()).append(
                    Component.text(
                                    "The Message Channel seems to be unavailable. " +
                                            "Hover for more information.")
                            .hoverEvent(Component.text("Targeted Channel: "+channel)
                                    .appendNewline()
                                    .append(Component.text("Message: ").append(message)))
            ));
            LionChat.sendDebugMessage(Component.text("The Message \"")
                    .append(message)
                    .append(Component.text("\" could not be sent, because the channel "+channel +
                            " has not been registered.")));
        }

    }

    public static void registerChannel(String channel, ChannelConfiguration config){
        channels.put(channel, config.addKey(channel));
    }

    public static HashMap<String, ChannelConfiguration> getChannels(){
        return channels;
    }

    private static Component getFullPrefix(Component name){
        return getFullPrefix(name, NamedTextColor.WHITE);
    }
    private static Component getFullPrefix(Component name, TextColor color){
        return name.appendSpace()
                .append(Component.text(">>", color))
                .appendSpace();
    }
}

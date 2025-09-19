package de.lioncraft.lionapi.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

public abstract class DM {
    public static final Component messagePrefix = getMessagePrefix();
    public static Component wrongArgs = messagePrefix.append(Component.text("Wrong usage of args.", TextColor.color(255, 0, 0)));
    public static Component noPlayer = messagePrefix.append(Component.text("Could not find this Player.", TextColor.color(255, 0, 0)));
    public static Component commandError = messagePrefix.append(Component.text("An error occurred when trying to execute this command.", TextColor.color(255, 0, 0)));
    public static Component noPermission = messagePrefix.append(Component.text("You do not have the permission to do this.", TextColor.color(255, 0, 0)));
    public static Component wait = messagePrefix.append(Component.text("Please wait a bit before doing that.", TextColor.color(255, 0, 0)));
    public static Component notAPlayer = messagePrefix.append(Component.text("This Command can only be executed as a Player!", TextColor.color(255, 0, 0)));

    /**Creates an Error Message with the LionSystems Prefix and
     * the {@link TextColor} Orange if none is previously set.
     * @param message the raw Message
     * @return the Styled Message
     */
    public static Component error(Component message){
        message = message.colorIfAbsent(TextColor.color(TextColor.color(255, 128, 0)));
        message = messagePrefix.append(message);
        return message;
    }
    /**Creates an Error Message with the LionSystems Prefix and
     * the {@link TextColor} Orange
     * @param message the raw Message
     * @return the Styled Message
     */
    public static Component error(String message){
        return error(Component.text(message));
    }
    /**Creates an Error Message with the LionSystems Prefix and
     * the {@link TextColor} Red if none is previously set.
     * @param message the raw Message
     * @return the Styled Message
     */
    public static Component fatalError(Component message){
        message = message.colorIfAbsent(TextColor.color(TextColor.color(255, 0, 0)));
        message = messagePrefix.append(message);
        return message;
    }
    /**Creates an Error Message with the LionSystems Prefix and
     * the {@link TextColor} Red
     * @param message the raw Message
     * @return the Styled Message
     */
    public static Component fatalError(String message){
        return fatalError(Component.text(message));
    }
    /**Creates a Message with the LionSystems Prefix
     * @param message the raw Message
     * @return the Styled Message
     */
    public static Component info(Component message){
        return messagePrefix.append(message);
    }
    /**Creates a Message with the LionSystems Prefix
     * @param message the raw Message
     * @return the Styled Message
     */
    public static Component info(String message){
        return info(Component.text(message));
    }
    /**Creates a Message with the LionSystems Prefix
     * @param seconds The time to wait in Seconds
     * @return the Styled Message
     */
    public static Component waitSeconds(@Nullable Integer seconds){
        if(seconds == null){
            return DM.wait;
        }else{

            if(seconds >= 60){
                return DM.messagePrefix.append(Component.text("Please try again in " + (int) seconds/60 + " Seconds!", TextColor.color(255, 128, 0)));
            }
            return DM.messagePrefix.append(Component.text("Please try again in " + seconds + " Seconds!", TextColor.color(255, 128, 0)));
        }
    }
    /**Creates a Message with the LionSystems Prefix and converts the Ticks into Seconds
     * @param ticks The time to wait in Ticks
     * @return the Styled Message
     */
    public static Component waitTicks(@Nullable Integer ticks){
        if(ticks == null){
            return DM.wait;
        }else{
            if(ticks < 20) ticks = 20;
            return waitSeconds(ticks / 20);
        }
    }

    private static Component getMessagePrefix(){
        return Component.text("<", TextColor.color(255, 255, 255))
                .append(Component.text("L", TextColor.color(255, 0, 255)))
                .append(Component.text("i", TextColor.color(220, 0, 255)))
                .append(Component.text("o", TextColor.color(190, 0, 255)))
                .append(Component.text("n", TextColor.color(160, 0, 255)))
                .append(Component.text("S", TextColor.color(130, 0, 255)))
                .append(Component.text("y", TextColor.color(100, 0, 255)))
                .append(Component.text("s", TextColor.color(70, 0, 255)))
                .append(Component.text("t", TextColor.color(40, 0, 255)))
                .append(Component.text("e", TextColor.color(10, 0, 255)))
                .append(Component.text("m", TextColor.color(0, 20, 255)))
                .append(Component.text("s", TextColor.color(0, 50, 255)))
                .append(Component.text("> ", TextColor.color(255, 255, 255)));
    }
}

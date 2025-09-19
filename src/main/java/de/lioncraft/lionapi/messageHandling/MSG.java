package de.lioncraft.lionapi.messageHandling;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public enum MSG{
    BETA(Component.text("Beta Features are not enabled!")),
    WRONG_ARGS(Component.text("Wrong usage of args!")),
    noPlayer (Component.text("Could not find this Player.", TextColor.color(255, 0, 0))),
    commandError (Component.text("An error occurred when trying to execute this command.", TextColor.color(255, 0, 0))),
    noPermission (Component.text("You do not have the permission to do this.", TextColor.color(255, 0, 0))),
    wait (Component.text("Please wait a bit before doing that.", TextColor.color(255, 0, 0))),
    NO_TEAM(Component.text("Seems like you are not in a Team")),

    notAPlayer (Component.text("This Command can only be executed as a Player!", TextColor.color(255, 0, 0)));


    private Component text;
    MSG(@NotNull Component text) {
        this.text = text;
    }

    public Component getText() {
        return text;
    }
}

package de.lioncraft.lionapi.messageHandling.lionchat;

import com.velocitypowered.api.proxy.Player;
import dev.lionk.lionVelocity.LionVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChannelConfiguration {
    private boolean opOnly;
    private List<UUID> ignoredBy = new ArrayList<>();
    private TextColor defaultColor = NamedTextColor.WHITE;
    private Component prefix = getMessagePrefix();
    private boolean configurableByNonOPs;
    private String key;

    public ChannelConfiguration(boolean opOnly, TextColor defaultColor, Component prefix, boolean configurableByNonOPs) {
        this.opOnly = opOnly;
        this.defaultColor = defaultColor;
        this.prefix =
                prefix
                .append(Component.text("", NamedTextColor.WHITE));
        this.configurableByNonOPs = configurableByNonOPs;
    }

    public ChannelConfiguration addKey(String key){
        this.key = key;
        return this;
    }

    public ChannelConfiguration() {
        opOnly = false;
        configurableByNonOPs = true;
    }

    public boolean canReceive(Player p){
        return !ignoredBy.contains(p.getUniqueId());
    }

    public boolean isOpOnly() {
        return opOnly;
    }
    public void setOpOnly(boolean opOnly) {
        this.opOnly = opOnly;
    }
    public List<UUID> getIgnoredBy() {
        return ignoredBy;
    }
    public void addIgnoredByPlayer(UUID player){
        if (!ignoredBy.contains(player)) ignoredBy.add(player);
    }
    public void removeIgnoredByPlayer(UUID player){
         ignoredBy.remove(player);
    }
    public TextColor getDefaultColor() {
        return defaultColor;
    }
    public void setDefaultColor(TextColor defaultColor) {
        this.defaultColor = defaultColor;
    }
    public Component getPrefix() {
        return prefix;
    }


    public void setPrefix(Component prefix) {
        this.prefix = prefix;
    }
    public boolean isConfigurableByNonOPs() {
        return configurableByNonOPs;
    }
    public void setConfigurableByNonOPs(boolean configurableByNonOPs) {
        this.configurableByNonOPs = configurableByNonOPs;
    }

    public List<Player> getPlayersToBroadcastTo(){
        List<Player> list = new ArrayList<>();
        for (Player p : LionVelocity.instance.getServer().getAllPlayers()){
            if (ignoredBy.contains(p.getUniqueId())) continue;
            list.add(p);
        }
        return list;
    }

    private static Component getMessagePrefix(){
        return Component.text("", TextColor.color(255, 255, 255))
                .append(Component.text("L", TextColor.color(255, 0, 255)))
                .append(Component.text("i", TextColor.color(220, 0, 255)))
                .append(Component.text("o", TextColor.color(200, 0, 255)))
                .append(Component.text("n", TextColor.color(180, 0, 255)))
                .append(Component.text("S", TextColor.color(160, 0, 255)))
                .append(Component.text("y", TextColor.color(140, 0, 255)))
                .append(Component.text("s", TextColor.color(120, 0, 255)))
                .append(Component.text("t", TextColor.color(100, 30, 255)))
                .append(Component.text("e", TextColor.color(90, 50, 255)))
                .append(Component.text("m", TextColor.color(80, 70, 255)))
                .append(Component.text("s", TextColor.color(70, 90, 255)))
                .append(Component.text(" >> ", TextColor.color(255, 255, 255)));
    }
}

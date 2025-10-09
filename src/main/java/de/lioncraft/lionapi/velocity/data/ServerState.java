package de.lioncraft.lionapi.velocity.data;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.Base64;
import java.util.Objects;

public class ServerState {
    private static final Gson gson = new Gson();
    private String name;
    private boolean enabled;
    private String motd;
    private String base64Favicon;
    private String stringItemStack;
    private Integer maxPlayers;
    private int currentPlayers;

    public ServerState(String name, boolean enabled, @Nullable String motd, @Nullable String base64Favicon, String stringItemStack, Integer maxPlayers, int currentPlayers) {
        this.name = name;
        this.enabled = enabled;
        this.motd = motd;
        this.base64Favicon = base64Favicon;
        this.stringItemStack = stringItemStack;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
    }

    public boolean updateEnabled(boolean newValue){
        boolean b = newValue != enabled;
        enabled = newValue;
        return b;
    }

    public boolean updateMOTD(String newValue){
        if (newValue == null) return false;
        boolean b = !Objects.equals(newValue, motd);
        motd = newValue;
        return b ;
    }

    public boolean updateFavicon(String newValue){
        if (newValue == null) return false;
        boolean b = !Objects.equals(newValue, base64Favicon);
        base64Favicon = newValue;
        return b;
    }

    public boolean updateMaxPlayers(Integer newValue){
        if (newValue == null) return false;
        boolean b = !Objects.equals(newValue, maxPlayers);
        maxPlayers = newValue;
        return b;
    }

    public boolean updateCurrentPlayers(int newValue){
        boolean b = !Objects.equals(newValue, currentPlayers);
        currentPlayers = newValue;
        return b;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return enabled;
    }

    public String getMotdString() {
        return motd;
    }

    public String getStringItemStack() {
        return stringItemStack;
    }

    public Component getMOTD() {
        return JSONComponentSerializer.json().deserialize(motd);
    }

    public String getBase64Favicon() {
        if (base64Favicon.contains("data:image/png;base64,"))
            base64Favicon = base64Favicon.replace("data:image/png;base64,", "");
        return base64Favicon;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void setMotd(String motd) {
        this.motd = motd;
    }

    private void setBase64Favicon(String base64Favicon) {
        this.base64Favicon = base64Favicon;
    }

    private void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    private void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }

    @Override
    public String toString(){
        return gson.toJson(this);
    }

    public static ServerState createServerState(TransferrableObject to){
        if (Objects.equals(to.getObjectType(), "LionLobby_ServerState")){
            return createServerState(to.getString("data"));
        } else throw new MalformedParametersException("The provided object is not a ServerState object and can't be deserialized");
    }
    public static ServerState createServerState(String json){
        return gson.fromJson(json, ServerState.class);
    }
}

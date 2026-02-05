package de.lioncraft.lionapi.velocity.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerConfiguration {
    private static final Gson gson = new GsonBuilder().create();
    public long timestamp = 0;
    public boolean isOperator = false;
    public long  lastOnline = 0;
    public boolean autoServerSwitch = false;
    public List<String> permissions = new ArrayList<String>();
    public HashMap<String, JsonElement> data = new HashMap<>();
    public final UUID uuid;

    public PlayerConfiguration(UUID uuid) {
        this.uuid = uuid;
        timestamp = System.currentTimeMillis();
    }

    public PlayerConfiguration(long timestamp, boolean isOperator, long lastOnline, boolean autoServerSwitch, ArrayList<String> permissions, HashMap<String, JsonElement> data, UUID uuid) {
        this.timestamp = timestamp;
        this.isOperator = isOperator;
        this.lastOnline = lastOnline;
        this.autoServerSwitch = autoServerSwitch;
        this.permissions = permissions;
        this.data = data;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return gson.toJson(this, PlayerConfiguration.class);
    }

    public static PlayerConfiguration fromJson(String json) {
        return gson.fromJson(json, PlayerConfiguration.class);
    }
}

package me.novoro.seam.objects;

import me.novoro.seam.api.Location;
import me.novoro.seam.api.configuration.Configuration;

import java.util.*;

public class PlayerData {
    public String username;
    public UUID uuid;
    public Location previousLocation;
    public boolean flyToggle;
    public boolean godToggle;
    public boolean nightVisionToggle;
    public boolean tpToggle;
    public boolean waterBreathingToggle;
    public long firstJoin = 0L;
    public long lastJoin = 0L;
    private final Map<String, Home> homes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.flyToggle = false;
        this.godToggle = false;
        this.nightVisionToggle = false;
        this.tpToggle = false;
        this.waterBreathingToggle = false;
        this.previousLocation = null;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getPreviousLocation() {
        return previousLocation;
    }

    public Boolean getFlyToggle() {
        return flyToggle;
    }

    public Boolean getGodToggle() {
        return godToggle;
    }

    public Boolean getNightVisionToggle() {
        return nightVisionToggle;
    }

    public Boolean getTpToggle() {
        return tpToggle;
    }

    public Boolean getWaterBreathingToggle() {
        return waterBreathingToggle;
    }

    public Home getHome(String name) {
        return this.homes.get(name);
    }

    public Map<String, Home> getHomes() {
        return this.homes;
    }

    public List<String> getHomeNames() {
        return new ArrayList<>(this.homes.keySet());
    }

    public void setHome(String name, Home home) {
        this.homes.put(name, home);
    }

    public boolean deleteHome(String name) {
        return this.homes.remove(name) != null;
    }

    public Configuration toConfiguration() {
        Configuration config = new Configuration();
        if (this.username != null) config.set("username", this.username);
        config.set("fly", this.flyToggle);
        config.set("god", this.godToggle);
        config.set("nightvision", this.nightVisionToggle);
        config.set("tp-toggle", this.tpToggle);
        config.set("water-breathing", this.waterBreathingToggle);
        if (this.firstJoin != 0L) config.set("first-join", this.firstJoin);
        if (this.lastJoin != 0L) config.set("last-join", this.lastJoin);
        if (this.previousLocation != null) config.set("previous-location", this.previousLocation.toConfiguration());
        if (!this.homes.isEmpty()) {
            Configuration homesConfig = new Configuration();
            for (Map.Entry<String, Home> entry : this.homes.entrySet()) {
                homesConfig.set(entry.getKey(), entry.getValue().toConfiguration());
            }
            config.set("homes", homesConfig);
        }
        return config;
    }

    public static PlayerData fromConfiguration(UUID uuid, Configuration config) {
        PlayerData data = new PlayerData(uuid);
        data.username = config.getString("username", null);
        data.flyToggle = config.getBoolean("fly", false);
        data.godToggle = config.getBoolean("god", false);
        data.nightVisionToggle = config.getBoolean("nightvision", false);
        data.tpToggle = config.getBoolean("tp-toggle", false);
        data.waterBreathingToggle = config.getBoolean("water-breathing", false);
        data.firstJoin = config.getLong("first-join", 0L);
        data.lastJoin = config.getLong("last-join", 0L);
        if (config.contains("previous-location")) data.previousLocation = config.getLocation("previous-location");
        if (config.contains("homes")) {
            Configuration homesConfig = config.getSection("homes");
            for (String key : homesConfig.getKeys()) {
                Configuration homeConfig = homesConfig.getSection(key);
                if (homeConfig != null) data.homes.put(key, new Home(key, homeConfig));
            }
        }
        return data;
    }
}

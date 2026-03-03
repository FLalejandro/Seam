package me.novoro.seam.objects;

import me.novoro.seam.api.Location;
import me.novoro.seam.api.configuration.Configuration;

import java.util.UUID;

public class PlayerData {
    public String username;
    public UUID uuid;
    public Location previousLocation;
    public boolean flyToggle;
    public boolean godToggle;
    public boolean nightVisionToggle;
    public boolean tpToggle;
    public boolean waterBreathingToggle;

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

    public Configuration toConfiguration() {
        Configuration config = new Configuration();
        if (this.username != null) config.set("username", this.username);
        config.set("fly", this.flyToggle);
        config.set("god", this.godToggle);
        config.set("nightvision", this.nightVisionToggle);
        config.set("tp-toggle", this.tpToggle);
        config.set("water-breathing", this.waterBreathingToggle);
        if (this.previousLocation != null) config.set("previous-location", this.previousLocation.toConfiguration());
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
        if (config.contains("previous-location")) data.previousLocation = config.getLocation("previous-location");
        return data;
    }
}

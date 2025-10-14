package me.novoro.seam.objects;

import me.novoro.seam.api.Location;
import java.util.HashMap;
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

}

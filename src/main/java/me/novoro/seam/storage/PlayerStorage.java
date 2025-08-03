package me.novoro.seam.storage;

import me.novoro.seam.api.Location;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class PlayerStorage {

    public final UUID playerUUID;
    private String playerName;
    public final HashMap<String, Location> homes = new HashMap<>();
    public boolean playedBefore = false;
    public boolean socialSpy = false;
    public boolean godMode = false;
    public boolean nightVision = false;
    public boolean waterBreathing = false;
    private Location previousLocation = null;
    private Location logoutLocation = null;
    private Instant lastTimeOnline = Instant.now();

    /**
     * Constructor to initialize PlayerStorage with a given UUID.
     *
     * @param uuid the UUID of the player.
     */
    public PlayerStorage(UUID uuid) {
        this.playerUUID = uuid;
        this.playedBefore = false;
        this.lastTimeOnline = Instant.now();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPreviousLocation(Location location) {
        this.previousLocation = location;
    }

    public Location getPreviousLocation() {
        return this.previousLocation;
    }

    public boolean getSocialSpy(){ return this.socialSpy; }
    public void setSocialSpy(boolean socialSpy) { this.socialSpy = socialSpy; }

    public boolean getGodMode() { return this.godMode;}
    public void setGodMode(boolean godMode) { this.godMode = godMode; }

    public boolean getNightVision() { return this.nightVision; }
    public void setNightVision(boolean nightVision) { this.nightVision = nightVision; }

    public boolean getWaterBreathing() { return this.waterBreathing; }
    public void setWaterBreathing(boolean waterBreathing) { this.waterBreathing = waterBreathing; }

    public Location getLogoutLocation() {
        return this.logoutLocation;
    }
    public void setLogoutLocation(Location location) {
        this.logoutLocation = location;
    }

    public Instant getLastTimeOnline() {
        return this.lastTimeOnline;
    }
    public void setLastTimeOnline() {
        this.lastTimeOnline = Instant.now();
    }

}
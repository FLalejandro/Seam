package me.novoro.seam.utils.randomteleport;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.utils.LocationUtil;
import me.novoro.seam.utils.RandomUtil;
import me.novoro.seam.utils.SeamLogger;
import net.minecraft.server.world.ServerWorld;

import java.util.concurrent.ThreadLocalRandom;

public class RTPWorldSettings {
    private final String worldName;
    private final String permission;
    private final int centerX;
    private final int centerZ;
    private final int minDistance;
    private final int maxDistance;
    private final boolean allowCaveTeleports;
    private final int highestY;
    private final int cooldown;

    public RTPWorldSettings(String worldName, Configuration config) {
        this.worldName = worldName;
        this.permission = config.getString("Permission", "seam.rtp");
        this.centerX = config.getInt("Center-X", 0);
        this.centerZ = config.getInt("Center-Z", 0);
        int min = config.getInt("Min-Distance", 250);
        int max = config.getInt("Max-Distance", 5000);
        if (min > max) {
            SeamLogger.warn("RTP world '" + worldName + "' has Min-Distance (" + min + ") > Max-Distance (" + max + "). Swapping values.");
            int tmp = min;
            min = max;
            max = tmp;
        }
        this.minDistance = min;
        this.maxDistance = max;
        this.allowCaveTeleports = config.getBoolean("Allow-Cave-Teleports", false);
        this.cooldown = config.getInt("Cooldown", 60);
        int highest = config.getInt("Highest-Y", 320);
        ServerWorld world = LocationUtil.getWorld(worldName);
        if (world != null && highest > world.getHeight()) {
            highest = world.getHeight();
        }
        this.highestY = highest;
    }

    /**
     * Returns a random XZ offset within the configured min/max distance ring.
     * Uses polar coordinates for a circular (radial) distribution.
     */
    public int[] getRandomOffset() {
        double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        int radius = RandomUtil.randomIntBetween(this.minDistance, this.maxDistance);
        int dx = (int) Math.round(Math.cos(angle) * radius);
        int dz = (int) Math.round(Math.sin(angle) * radius);
        return new int[]{dx, dz};
    }

    public String getWorldName() {
        return worldName;
    }

    public String getPermission() {
        return permission;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public boolean isAllowCaveTeleports() {
        return allowCaveTeleports;
    }

    public int getHighestY() {
        return highestY;
    }

    public int getCooldown() {
        return cooldown;
    }

    public ServerWorld getWorld() {
        return LocationUtil.getWorld(this.worldName);
    }
}

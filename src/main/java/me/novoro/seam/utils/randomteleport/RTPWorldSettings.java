package me.novoro.seam.utils.randomteleport;
 
import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.utils.LocationUtil;
import me.novoro.seam.utils.RandomUtil;
import net.minecraft.server.world.ServerWorld;

public class RTPWorldSettings {
    private final String worldName;
    private final String permission;
    private final int centerX;
    private final int centerZ;
    private final int minDistance;
    private final int maxDistance;
    private final boolean allowCaveTeleports;
    private final int highestY;
    
    public RTPWorldSettings(String worldName, Configuration config) {
        this.worldName = worldName;
        this.permission = config.getString("Permission", "seam.rtp");
        this.centerX = config.getInt("Center-X", 0);
        this.centerZ = config.getInt("Center-Z", 0);
        this.minDistance = config.getInt("Min-Distance", 250);
        this.maxDistance = config.getInt("Max-Distance", 5000);
        this.allowCaveTeleports = config.getBoolean("Allow-Cave-Teleports", false);
        this.highestY = config.getInt("Highest-Y", 320);
    }
    
    public int getRandomIntInBounds() {
        int offset = RandomUtil.randomIntBetween(this.minDistance, this.maxDistance);
        return RandomUtil.randomBoolean() ? offset : -offset;
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
    
    public int getHighestY() { 
        return highestY; 
    }

    public int getMaxDistance() { 
        return maxDistance; 
    }
    public boolean isAllowCaveTeleports() { 
        return allowCaveTeleports; 
    }

    public ServerWorld getWorld() {
        return LocationUtil.getWorld(this.worldName);
    }

    public boolean isInBounds(int x, int z) {
        x = Math.abs(x);
        z = Math.abs(z);
        return x >= minDistance && x <= maxDistance && z >= minDistance && z <= maxDistance;
    }

}

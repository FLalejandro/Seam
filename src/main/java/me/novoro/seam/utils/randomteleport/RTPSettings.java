package me.novoro.seam.utils.randomteleport;
 
import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.utils.SeamLogger;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.*;
 
public final class RTPSettings {
    private RTPSettings() {}

    private static final Map<String, RTPWorldSettings> worldSettings = new HashMap<>();
    private static final List<String> nonRedirectedWorlds = new ArrayList<>();
    private static int maxAttempts = 10;
    private static Set<Identifier> blacklistedBiomes = new HashSet<>();

    public static void reload(Configuration config) {
        worldSettings.clear();
        nonRedirectedWorlds.clear();
        blacklistedBiomes.clear();
        
        Configuration rtpSection = config.getSection("Random-Teleport");
        if (rtpSection == null) {
            SeamLogger.warn("Random-Teleport section not found in config, using defaults");
            return;
        }
        
        maxAttempts = rtpSection.getInt("Max-Attempts", 10);

        Configuration worldsConfig = rtpSection.getSection("Worlds");
        if (worldsConfig == null) {
            SeamLogger.warn("No 'Worlds' section found in Random-Teleport config");
            return;
        }

        Map<String, String> redirectedWorlds = new HashMap<>();
        for (String worldName : worldsConfig.getKeys()) {
            Configuration worldConfig = worldsConfig.getSection(worldName);
            if (worldConfig == null) continue;
            if(!worldConfig.contains("Redirect-To")) {
                worldSettings.put(worldName, new RTPWorldSettings(worldName, worldConfig));
                nonRedirectedWorlds.add(worldName);
            } else {
                redirectedWorlds.put(worldName, worldConfig.getString("Redirect-To"));
            }
        }

        for (Map.Entry<String, String> redirect : redirectedWorlds.entrySet()) {
            RTPWorldSettings target = worldSettings.get(redirect.getValue());
            if (target != null) worldSettings.put(redirect.getKey(), target);
        }

        blacklistedBiomes = new HashSet<>();
        for (String id : rtpSection.getStringList("Blacklisted-Biomes")) {
            blacklistedBiomes.add(Identifier.of(id));
        }
        
    }

    public static int getMaxAttempts() {
        return maxAttempts;
    }

    public static List<String> getNonRedirectedWorlds() {
        return nonRedirectedWorlds;
    }

    public static Set<Identifier> getBlacklistedBiomes() {
        return blacklistedBiomes;
    }

    public static boolean isBiomeBlacklisted(Identifier biomeId) {
        return blacklistedBiomes.contains(biomeId);
    }
    
    public static RTPWorldSettings getWorldSettings(ServerWorld world) {
        return getWorldSettings(world.getRegistryKey().getValue().toString());
    }
    
    public static RTPWorldSettings getWorldSettings(String worldName) {
        return worldSettings.get(worldName);
    }

}

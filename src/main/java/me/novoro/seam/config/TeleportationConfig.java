package me.novoro.seam.config;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;
import me.novoro.seam.utils.LocationUtil;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seam's teleport config settings.
 */
public class TeleportationConfig extends VersionedConfig {
    // Block IDs of blocks that are unsafe to teleport on top of.
    private static final List<String> UNSAFE_BLOCKS = new ArrayList<>();
    // Block IDs of blocks that Seam will count as air.
    private static final List<String> AIR_BLOCKS = new ArrayList<>();
    // The highest Y value that ascend and top can teleport to.
    private static final Map<String, Integer> ASCEND_MAX_Y_VALUES = new HashMap<>();

    @Override
    protected void reload(Configuration config) {
        super.reload(config);
        TeleportationConfig.UNSAFE_BLOCKS.clear();
        TeleportationConfig.UNSAFE_BLOCKS.addAll(config.getStringList("Unsafe-Blocks"));
        TeleportationConfig.AIR_BLOCKS.clear();
        TeleportationConfig.AIR_BLOCKS.addAll(config.getStringList("Air-Blocks"));
        TeleportationConfig.ASCEND_MAX_Y_VALUES.clear();
        Configuration ascendMaxYSection = config.getSection("Ascend-Max-Y-Values");
        if (ascendMaxYSection != null) {
            for (String key : ascendMaxYSection.getKeys()) {
                TeleportationConfig.ASCEND_MAX_Y_VALUES.put(key, ascendMaxYSection.getInt(key, 320));
            }
        }



    }

    public static boolean isBlockSafe(String blockID) {
        return !TeleportationConfig.UNSAFE_BLOCKS.contains(blockID);
    }

    public static boolean isAirBlock(String blockID) {
        return TeleportationConfig.AIR_BLOCKS.contains(blockID);
    }

    public static int getHighestAscendY(ServerWorld world) {
        String worldID = LocationUtil.getWorldName(world);
        return TeleportationConfig.ASCEND_MAX_Y_VALUES.computeIfAbsent(worldID,
                __ -> TeleportationConfig.ASCEND_MAX_Y_VALUES.getOrDefault("default", 320));
    }

    @Override
    protected double getCurrentConfigVersion() {
        return 1.0;
    }

    @Override
    protected String getConfigFileName() {
        return "teleportation.yml";
    }
}

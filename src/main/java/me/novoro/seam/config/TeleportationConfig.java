package me.novoro.seam.config;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;
import me.novoro.seam.utils.LocationUtil;
import me.novoro.seam.utils.SeamLogger;
import me.novoro.seam.utils.randomteleport.RTPSettings;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Seam's teleport config settings.
 */
public class TeleportationConfig extends VersionedConfig {
    // Blocks that are unsafe to teleport on top of.
    private static final Set<Block> UNSAFE_BLOCKS = new HashSet<>();
    // Blocks that Seam will count as air.
    private static final Set<Block> AIR_BLOCKS = new HashSet<>();
    // The highest Y value that ascend and top can teleport to.
    private static final Map<String, Integer> ASCEND_MAX_Y_VALUES = new HashMap<>();

    @Override
    protected void reload(Configuration config) {
        super.reload(config);
        UNSAFE_BLOCKS.clear();
        for (String id : config.getStringList("Unsafe-Blocks")) {
            Identifier blockId = Identifier.of(id);
            if (Registries.BLOCK.containsId(blockId)) {
                UNSAFE_BLOCKS.add(Registries.BLOCK.get(blockId));
            } else {
                SeamLogger.warn("Unknown block ID in Unsafe-Blocks: " + id);
            }
        }
        AIR_BLOCKS.clear();
        for (String id : config.getStringList("Air-Blocks")) {
            Identifier blockId = Identifier.of(id);
            if (Registries.BLOCK.containsId(blockId)) {
                AIR_BLOCKS.add(Registries.BLOCK.get(blockId));
            } else {
                SeamLogger.warn("Unknown block ID in Air-Blocks: " + id);
            }
        }
        ASCEND_MAX_Y_VALUES.clear();
        Configuration ascendMaxYSection = config.getSection("Ascend-Max-Y-Values");
        if (ascendMaxYSection != null) {
            for (String key : ascendMaxYSection.getKeys()) {
                ASCEND_MAX_Y_VALUES.put(key, ascendMaxYSection.getInt(key, 320));
            }
        }
        RTPSettings.reload(config);
    }

    public static boolean isBlockSafe(Block block) {
        return !UNSAFE_BLOCKS.contains(block);
    }

    public static boolean isAirBlock(Block block) {
        return AIR_BLOCKS.contains(block);
    }

    public static int getHighestAscendY(ServerWorld world) {
        String worldID = LocationUtil.getWorldName(world);
        return ASCEND_MAX_Y_VALUES.getOrDefault(worldID,
                ASCEND_MAX_Y_VALUES.getOrDefault("default", 320));
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

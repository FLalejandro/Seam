package me.novoro.seam.config;

import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;
import me.novoro.seam.utils.LocationUtil;
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
    // Blocks that Seam counts as air.
    private static final Set<Block> AIR_BLOCKS = new HashSet<>();
    // The highest Y value that ascend and top can teleport to.
    private static final Map<String, Integer> ASCEND_MAX_Y_VALUES = new HashMap<>();

    @Override
    protected void reload(Configuration config) {
        super.reload(config);
        TeleportationConfig.UNSAFE_BLOCKS.clear();
        for (String id : config.getStringList("Unsafe-Blocks")) {
            Registries.BLOCK.getOrEmpty(Identifier.of(id)).ifPresent(TeleportationConfig.UNSAFE_BLOCKS::add);
        }
        TeleportationConfig.AIR_BLOCKS.clear();
        for (String id : config.getStringList("Air-Blocks")) {
            Registries.BLOCK.getOrEmpty(Identifier.of(id)).ifPresent(TeleportationConfig.AIR_BLOCKS::add);
        }
        TeleportationConfig.ASCEND_MAX_Y_VALUES.clear();
        Configuration ascendMaxYSection = config.getSection("Ascend-Max-Y-Values");
        if (ascendMaxYSection != null) {
            for (String key : ascendMaxYSection.getKeys()) {
                TeleportationConfig.ASCEND_MAX_Y_VALUES.put(key, ascendMaxYSection.getInt(key, 320));
            }
        }

        RTPSettings.reload(config);

    }

    public static boolean isBlockSafe(Block block) {
        return !TeleportationConfig.UNSAFE_BLOCKS.contains(block);
    }

    public static boolean isAirBlock(Block block) {
        return TeleportationConfig.AIR_BLOCKS.contains(block);
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

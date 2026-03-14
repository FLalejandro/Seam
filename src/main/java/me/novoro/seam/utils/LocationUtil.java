package me.novoro.seam.utils;

import me.novoro.seam.Seam;
import me.novoro.seam.api.Location;
import me.novoro.seam.config.TeleportationConfig;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/**
 * Provides various Location utility methods to Seam.
 */
public final class LocationUtil {
    /**
     * Gets a {@link ServerWorld} via a world name.
     * @param worldName The name of the world you're attempting to obtain.
     * @return The {@link ServerWorld} with the specified world name.
     */

    public static ServerWorld getWorld(String worldName) {
        for (ServerWorld serverWorld : Seam.getServer().getWorlds()) {
            if (!LocationUtil.getWorldName(serverWorld).equals(worldName)) continue;
            return serverWorld;
        }
        return null;
    }

    /**
     * Gets the world name of a specified {@link ServerWorld}.
     */
    public static String getWorldName(ServerWorld world) {
        return world.getRegistryKey().getValue().toString();
    }

    /**
     * Gets the {@link Location} that the player is looking at.
     * @param player The target {@link ServerPlayerEntity}.
     * @return The {@link Location} the target player is looking at.
     */
    public static Location getLookingAt(ServerPlayerEntity player) {
        HitResult hitResult = player.raycast(500, 0, false);
        Vec3d target = hitResult.getPos();
        return new Location(player.getServerWorld(), target.x, target.y, target.z);
    }

    /**
     * Gets the next safe {@link Location} below the player.
     */
    public static Location getNextSafeBelow(Location location, boolean highestOnly) {
        boolean air1 = false, air2 = false;
        int bottomY = location.getWorld().getBottomY();
        Location copy = location.copy().shifted(0, -1, 0);
        while (copy.getY() > bottomY) {
            String blockID = Registries.BLOCK.getId(copy.getBlockState().getBlock()).toString();
            if (TeleportationConfig.isAirBlock(blockID)) {
                if (air1) air2 = true;
                air1 = true;
            } else if (air1 && air2 && TeleportationConfig.isBlockSafe(blockID)) {
                return copy.shifted(0, 1, 0);
            } else {
                if (highestOnly) return null;
                air1 = air2 = false;
            }
            copy.shift(0, -1, 0);
        }
        return null;
    }

    /**
     * Gets the next safe {@link Location} above the player.
     */
    public static Location getNextSafeAbove(Location location) {
        boolean block = false, air = false;
        int worldHeight = location.getWorld().getHeight();
        Location copy = location.copy().shifted(0, 1, 0);
        while (copy.getY() < worldHeight) {
            String blockID = Registries.BLOCK.getId(copy.getBlockState().getBlock()).toString();
            boolean isAirBlock = TeleportationConfig.isAirBlock(blockID);
            if (block && air && isAirBlock) {
                return copy.shifted(0, -1, 0);
            } else if (!isAirBlock) {
                block = true;
                air = false;
            } else if (block) air = true;
            copy.shift(0, 1, 0);
        }
        return null;
    }
}
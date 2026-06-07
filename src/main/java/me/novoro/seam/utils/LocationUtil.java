package me.novoro.seam.utils;

import me.novoro.seam.Seam;
import me.novoro.seam.api.Location;
import me.novoro.seam.config.TeleportationConfig;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

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
            Block block = copy.getBlockState().getBlock();
            if (TeleportationConfig.isAirBlock(block)) {
                if (air1) air2 = true;
                air1 = true;
            } else if (air1 && air2 && TeleportationConfig.isBlockSafe(block)) {
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
        boolean isBlock = false, isAir = false;
        int worldHeight = location.getWorld().getHeight();
        Location copy = location.copy().shifted(0, 1, 0);
        while (copy.getY() < worldHeight) {
            Block b = copy.getBlockState().getBlock();
            boolean airBlock = TeleportationConfig.isAirBlock(b);
            if (isBlock && isAir && airBlock) {
                return copy.shifted(0, -1, 0);
            } else if (!airBlock) {
                isBlock = true;
                isAir = false;
            } else if (isBlock) isAir = true;
            copy.shift(0, 1, 0);
        }
        return null;
    }

    /**
     * Scans downward from startY to find a safe {@link Location} for RTP.
     * Does not force chunk generation. Ceiling dimensions fall back to cave scanning.
     */
    public static Location findSafeRTPLocation(ServerWorld world, int x, int startY, int z, boolean allowCaves) {
        BlockPos.Mutable pos = new BlockPos.Mutable(x, startY, z);
        WorldChunk chunk = (WorldChunk) world.getChunkManager().getChunk(x >> 4, z >> 4, ChunkStatus.EMPTY, false);
        if (chunk == null) return null;

        boolean useCaveScan = allowCaves || world.getDimension().hasCeiling();
        int scanFrom = useCaveScan ? startY
                : chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(x & 15, z & 15);
        for (int y = scanFrom; y > world.getBottomY() + 1; y--) {
            pos.setY(y);
            Block feet = chunk.getBlockState(pos).getBlock();
            if (!TeleportationConfig.isAirBlock(feet)) continue;
            pos.setY(y + 1);
            Block head = chunk.getBlockState(pos).getBlock();
            if (!TeleportationConfig.isAirBlock(head)) continue;
            pos.setY(y - 1);
            Block floor = chunk.getBlockState(pos).getBlock();
            if (TeleportationConfig.isAirBlock(floor) || !TeleportationConfig.isBlockSafe(floor)) continue;
            return new Location(world, x + 0.5, y, z + 0.5);
        }
        return null;
    }
}
package me.novoro.seam.utils;

import me.novoro.seam.Seam;
import me.novoro.seam.api.Location;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public final class LocationUtil {
    /**
     * Gets a {@link ServerWorld} via a world name.
     * @param worldName The name of the world you're attempting to obtain.
     * @return The {@link ServerWorld} with the specified world name.
     */

    public static ServerWorld getWorld(String worldName) {
        for (ServerWorld serverWorld : Seam.getServer().getWorlds()) {
            if (!serverWorld.getRegistryKey().getValue().toString().equals(worldName)) continue;
            return serverWorld;
        }
        return null;
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
}
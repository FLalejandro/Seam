package me.novoro.seam.utils;

import com.mojang.authlib.GameProfile;
import me.novoro.seam.Seam;
import me.novoro.seam.api.Location;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

//TODO: Use this for offline inventory and enderchest viewing. LastDeath if that is ever relevant. Maybe for /back
public class GameProfileUtil {

    /**
     * Gets the player data file for a given GameProfile.
     * Used for Offline player instances.
     */
    public static File getPlayerDataFile(GameProfile profile) {
        MinecraftServer server = Seam.getServer();
        File playerDir = server.getSavePath(WorldSavePath.PLAYERDATA).toFile();
        return new File(playerDir, profile.getId().toString() + ".dat");
    }

    /**
     * Reads the root NBT compound for an offline player.
     */
    public static NbtCompound getOfflineData(GameProfile profile) throws IOException {
        File file = getPlayerDataFile(profile);
        if (!file.exists()) return null;

        try (InputStream is = Files.newInputStream(file.toPath())) {
            return NbtIo.readCompressed(is, NbtSizeTracker.of(2097152L));
        }
    }

    /**
     * Extracts the last known Location of an offline player.
     */
    public static Location getOfflineLocation(GameProfile profile) throws IOException {
        NbtCompound nbt = getOfflineData(profile);
        if (nbt == null) return null;

        // Position
        NbtList posList = nbt.getList("Pos", NbtElement.DOUBLE_TYPE);
        double x = posList.getDouble(0);
        double y = posList.getDouble(1);
        double z = posList.getDouble(2);

        // Pitch and Yaw
        NbtList rotationList = nbt.getList("Rotation", NbtElement.FLOAT_TYPE);
        float pitch = rotationList.getFloat(0);
        float yaw = rotationList.getFloat(1);

        // World
        MinecraftServer server = Seam.getServer();
        ServerWorld world = getOfflineWorld(server, nbt);
        if (world == null) return null;

        return new Location(world, x, y, z, pitch, yaw);
    }

    /**
     * Resolves the correct world from Dimension info in the NBT.
     */
    private static ServerWorld getOfflineWorld(MinecraftServer server, NbtCompound nbt) {
        if (nbt.contains("Dimension", NbtElement.STRING_TYPE)) {
            String dimension = nbt.getString("Dimension");
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimension));
            return server.getWorld(key);
        }
        return server.getOverworld();
    }

}

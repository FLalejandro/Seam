package me.novoro.seam.config;

import me.novoro.seam.Seam;
import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.YamlConfiguration;
import me.novoro.seam.objects.PlayerData;
import me.novoro.seam.utils.SeamLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStorageManager {
    private static final Map<UUID, PlayerData> PLAYER_DATA = new HashMap<>();

    public static PlayerData get(UUID uuid) {
        return PLAYER_DATA.computeIfAbsent(uuid, PlayerStorageManager::load);
    }

    public static void set(UUID uuid, PlayerData data) {
        PLAYER_DATA.put(uuid, data);
    }

    public static Map<UUID, PlayerData> getAll() {
        return PLAYER_DATA;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getPlayerFile(UUID uuid) {
        File playersDir = new File(Seam.inst().getDataFolder(), "players");
        if (!playersDir.exists()) playersDir.mkdirs();
        return new File(playersDir, uuid.toString() + ".yml");
    }

    public static PlayerData load(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) return new PlayerData(uuid);
        try {
            Configuration config = YamlConfiguration.loadConfiguration(file);
            return PlayerData.fromConfiguration(uuid, config);
        } catch (IOException e) {
            SeamLogger.error("Failed to load player data for " + uuid + ".");
            SeamLogger.printStackTrace(e);
            return new PlayerData(uuid);
        }
    }

    public static void save(UUID uuid) {
        PlayerData data = PLAYER_DATA.get(uuid);
        if (data == null) return;
        File file = getPlayerFile(uuid);
        try {
            YamlConfiguration.save(data.toConfiguration(), file);
        } catch (IOException e) {
            SeamLogger.error("Failed to save player data for " + uuid + ".");
            SeamLogger.printStackTrace(e);
        }
    }

    public static void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : PLAYER_DATA.entrySet()) {
            save(entry.getKey());
        }
    }

    public static void remove(UUID uuid) {
        PLAYER_DATA.remove(uuid);
    }
    
    public static PlayerData findbyUsername(String username) {
        for (PlayerData data : PLAYER_DATA.values()) {
            if (data.getUsername().equalsIgnoreCase(username)) return data;
        }
        File playersDir = new File(Seam.inst().getDataFolder(), "players");
        if (!playersDir.exists()) return null;
        File[] files = playersDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return null;
        for (File file : files) {
            try {
                Configuration config = YamlConfiguration.loadConfiguration(file);
                String usernameFromConfig = config.getString("username");
                if (username.equalsIgnoreCase(usernameFromConfig)) {
                    UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    return PlayerData.fromConfiguration(uuid, config);
                }
            } catch (IOException | IllegalArgumentException e) {
                SeamLogger.error("Failed to load player data for " + file.getName() + ".");
                SeamLogger.printStackTrace(e);
            }
        }
        return null;
    }
}

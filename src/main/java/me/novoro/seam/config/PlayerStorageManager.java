package me.novoro.seam.config;

import me.novoro.seam.objects.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStorageManager {
    private static final Map<UUID, PlayerData> PLAYER_DATA = new HashMap<>();

    public static PlayerData get(UUID uuid) {
        return PLAYER_DATA.computeIfAbsent(uuid, PlayerData::new);
    }

    public static void set(UUID uuid, PlayerData data) {
        PLAYER_DATA.put(uuid, data);
    }

    public static Map<UUID, PlayerData> getAll() {
        return PLAYER_DATA;
    }

}

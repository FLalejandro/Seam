package me.novoro.seam.config;

import me.novoro.seam.Seam;
import me.novoro.seam.api.configuration.Configuration;
import me.novoro.seam.api.configuration.VersionedConfig;
import me.novoro.seam.api.permissions.SeamPermission;
import me.novoro.seam.objects.Waypoint;
import me.novoro.seam.utils.RandomUtil;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaypointManager extends VersionedConfig {
    private static final HashMap<String, Waypoint> SPAWNS = new HashMap<>();
    private static final HashMap<String, Waypoint> WARPS = new HashMap<>();
    private static Waypoint firstJoinSpawn = null;
    private static boolean spawnNoRespawn = true;
    private static boolean forceSpawnOnJoin = false;
    private static boolean forceSpawnOnDeath = false;

    @Override
    protected void reload(Configuration waypointConfig) {
        super.reload(waypointConfig);
        String firstJoinSpawnName = waypointConfig.getString("First-Join-Spawn", "");
        WaypointManager.firstJoinSpawn = WaypointManager.getSpawn(firstJoinSpawnName);
        WaypointManager.spawnNoRespawn = waypointConfig.getBoolean("Spawn-No-Respawn", true);
        WaypointManager.forceSpawnOnJoin = waypointConfig.getBoolean("Force-Spawn-On-Join", false);
        WaypointManager.forceSpawnOnDeath = waypointConfig.getBoolean("Force-Spawn-On-Death", false);

        Configuration spawnsSection = waypointConfig.getSection("Spawns");
        if (spawnsSection != null) {
            for (String spawnName : spawnsSection.getKeys()) {
                Configuration spawnConfig = spawnsSection.getSection(spawnName);
                if (spawnConfig != null) SPAWNS.put(spawnName, new Waypoint(spawnName, spawnConfig));
            }
        }

        Configuration warpsSection = waypointConfig.getSection("Warps");
        if (warpsSection != null) {
            for (String warpName : warpsSection.getKeys()) {
                Configuration warpConfig = warpsSection.getSection(warpName);
                if (warpConfig != null) WARPS.put(warpName, new Waypoint(warpName, warpConfig));

            }
        }
    }

    public static Waypoint getFirstJoinSpawn() {
        return WaypointManager.firstJoinSpawn;
    }

    public static boolean shouldForceSpawnOnJoin() {
        return WaypointManager.forceSpawnOnJoin;
    }

    public static boolean forceSpawnOnDeath() {
        return WaypointManager.forceSpawnOnDeath;
    }

    public static boolean spawnNoRespawn() {
        return WaypointManager.spawnNoRespawn;
    }

    public static boolean hasSpawn(String spawnName) {
        return SPAWNS.containsKey(spawnName);
    }

    public static Waypoint getSpawn(String spawnName) {
        return SPAWNS.get(spawnName);
    }

    public static List<String> getAllSpawnNames() {
        return new ArrayList<>(SPAWNS.keySet());
    }

    public static Waypoint determineSpawnPoint(ServerPlayerEntity player) {
        List<Waypoint> possibleTeleports = new ArrayList<>();
        int highestPriority = 0;
        for (Map.Entry<String, Waypoint> entry : WaypointManager.SPAWNS.entrySet()) {
            if (!SeamPermission.of("seam.spawn." + entry.getKey(), 1).matches(player)) continue;
            Waypoint spawn = entry.getValue();
            if (spawn.getWeight() > highestPriority) {
                highestPriority = spawn.getWeight();
                possibleTeleports.clear();
                possibleTeleports.add(spawn);
            } else if (spawn.getWeight() == highestPriority) possibleTeleports.add(spawn);
        }
        if (possibleTeleports.isEmpty()) return null;
        return RandomUtil.getRandomValue(possibleTeleports);
    }

    public static Waypoint createSpawn(ServerPlayerEntity player, String spawnName, int weight, String permission) {
        Waypoint newSpawn = new Waypoint(player, spawnName, weight, permission);
        WaypointManager.SPAWNS.put(spawnName, newSpawn);
        WaypointManager.saveConfig();
        return newSpawn;
    }

    public static void deleteSpawn(String spawnName) {
        WaypointManager.SPAWNS.remove(spawnName);
        WaypointManager.saveConfig();
    }

    public static void loadSpawns(Configuration config) {
        WaypointManager.SPAWNS.clear();
        Configuration WaypointsConfig = config.getSection("Spawns");
        if (WaypointsConfig == null) return;
        for (String key : WaypointsConfig.getKeys()) {
            Configuration spawnConfig = WaypointsConfig.getSection(key);
            if (spawnConfig == null) continue;
            WaypointManager.SPAWNS.put(key, new Waypoint(key, spawnConfig));
        }
    }

    public static boolean hasWarp(String warpName) {
        return WARPS.containsKey(warpName);
    }

    public static Waypoint getWarp(String warpName) {
        return WARPS.get(warpName);
    }

    public static List<String> getAllWarpNames() {
        return new ArrayList<>(WARPS.keySet());
    }

    public static Waypoint createWarp(ServerPlayerEntity player, String warpName, int weight, String permission) {
        Waypoint newWarp = new Waypoint(player, warpName, 0, permission);
        WaypointManager.WARPS.put(warpName, newWarp);
        WaypointManager.saveConfig();
        return newWarp;
    }

    public static void deleteWarp(String warpName) {
        WaypointManager.WARPS.remove(warpName);
        WaypointManager.saveConfig();
    }

    public static void loadWarps(Configuration config) {
        WaypointManager.WARPS.clear();
        Configuration WaypointsConfig = config.getSection("Warps");
        if (WaypointsConfig == null) return;
        for (String key : WaypointsConfig.getKeys()) {
            Configuration warpConfig = WaypointsConfig.getSection(key);
            if (warpConfig == null) continue;
            WaypointManager.WARPS.put(key, new Waypoint(key, warpConfig));
        }
    }

    private static void saveConfig() {
        Configuration config = new Configuration();

        config.set("First-Join-Spawn", firstJoinSpawn);
        config.set("Spawn-No-Respawn", spawnNoRespawn);
        config.set("Force-Spawn-On-Death", forceSpawnOnDeath);
        config.set("Force-Spawn-On-Join", forceSpawnOnJoin);

        Configuration spawnsSection = new Configuration();
        for (Map.Entry<String, Waypoint> entry : SPAWNS.entrySet()) {
            spawnsSection.set(entry.getKey(), entry.getValue().toConfiguration());
        }
        config.set("Spawns", spawnsSection);

        Configuration warpsSection = new Configuration();
        for (Map.Entry<String, Waypoint> entry : WARPS.entrySet()) {
            warpsSection.set(entry.getKey(), entry.getValue().toConfiguration());
        }
        config.set("Warps", warpsSection);

        Seam.inst().saveConfig("waypoints.yml", config);
    }

    @Override
    protected double getCurrentConfigVersion() {
        return 1.0;
    }

    @Override
    protected String getConfigFileName() {
        return "waypoints.yml";
    }
}
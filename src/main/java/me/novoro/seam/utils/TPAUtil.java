package me.novoro.seam.utils;

import me.novoro.seam.api.Location;
import me.novoro.seam.api.async.ServerScheduler;
import me.novoro.seam.config.LangManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

import me.novoro.seam.config.SettingsManager;

public final class TPAUtil {

    //TODO: TPToggle should be hooked to player storage
    private static final Set<UUID> isTPToggled = new HashSet<>();
    private static final Map<UUID, List<TeleportRequest>> teleportRequests = new HashMap<>();

    public static class TeleportRequest {
        final UUID senderUuid;
        final UUID targetUuid;
        final Location tpLocation;
        final TeleportType type;
        final long timestamp;

        public TeleportRequest(ServerPlayerEntity requester, ServerPlayerEntity target, Location tpLocation, TeleportType type, long timestamp) {
            this.senderUuid = requester.getUuid();
            this.targetUuid = target.getUuid();
            this.tpLocation = tpLocation;
            this.type = type;
            this.timestamp = timestamp;
        }
    }

    public enum TeleportType {
        TPA,
        TPAHERE
    }

    /*
     * Create Teleport Requests
     */
    public static void createTeleportRequest(ServerPlayerEntity sender, ServerPlayerEntity target, Location tpLocation, TeleportType type) {
        UUID targetUuid = target.getUuid();
        List<TeleportRequest> requests = teleportRequests.getOrDefault(targetUuid, new ArrayList<>());
        requests.add(new TeleportRequest(sender, target, tpLocation, type, System.currentTimeMillis()));
        teleportRequests.put(targetUuid, requests);
    }

    /*
     * Handle Teleport Request Response (Accept/Deny)
     */
    public static void handleTeleportRequest(ServerPlayerEntity target, boolean accepted) {
        UUID targetUuid = target.getUuid();
        List<TeleportRequest> requests = teleportRequests.get(targetUuid);
        if (requests == null || requests.isEmpty()) {
            LangManager.sendLang(target, "TPA-Request-None");
            return;
        }

        TeleportRequest request = requests.getFirst();
        removeTeleportRequest(request.senderUuid, targetUuid);

        ServerPlayerEntity sender = Objects.requireNonNull(target.getServer()).getPlayerManager().getPlayer(request.senderUuid);

        assert sender != null;
        if (accepted) {
            switch (request.type) {
                case TPA -> request.tpLocation.teleport(sender);
                case TPAHERE -> request.tpLocation.teleport(target);
            }
            LangManager.sendLang(sender, "TPA-Accept", Map.of("{player}", target.getName().getString()));
            LangManager.sendLang(target, "TPA-Accept", Map.of("{player}", sender.getName().getString()));
        } else {
            LangManager.sendLang(sender, "TPA-Deny", Map.of("{player}", target.getName().getString()));
            LangManager.sendLang(target, "TPA-Deny", Map.of("{player}", sender.getName().getString()));
        }
    }

    /*
     * Handles Teleport Timeouts
     */
    public static void handleTeleportTimeouts(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = SettingsManager.getTpaRequestTimeoutMillis();


        new HashMap<>(teleportRequests).forEach((targetUuid, requests) -> {
            List<TeleportRequest> expiredRequests = requests.stream()
                    .filter(request -> currentTime - request.timestamp >= timeoutMillis)
                    .toList();

            for (TeleportRequest expiredRequest : expiredRequests) {
                removeTeleportRequest(expiredRequest.senderUuid, expiredRequest.targetUuid);

                ServerScheduler.runSync(server, () -> {
                    ServerPlayerEntity sender = server.getPlayerManager().getPlayer(expiredRequest.senderUuid);
                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetUuid);

                    assert target != null;
                    assert sender != null;
                    LangManager.sendLang(sender, "TPA-Sender-Timeout", Map.of("{player}", target.getName().getString()));
                    LangManager.sendLang(target, "TPA-Target-Timeout", Map.of("{player}", sender.getName().getString()));
                });
            }
        });
    }

    /*
     * Removes Teleport Requests from the List once Accepted/Timed Out
     */
    public static void removeTeleportRequest(UUID senderUuid, UUID targetUuid) {
        List<TeleportRequest> requests = teleportRequests.get(targetUuid);
        if (requests != null) {
            requests.removeIf(request -> request.senderUuid.equals(senderUuid));
            if (requests.isEmpty()) teleportRequests.remove(targetUuid);
        }
    }
}

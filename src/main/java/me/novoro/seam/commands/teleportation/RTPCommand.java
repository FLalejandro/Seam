package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.api.Location;
import me.novoro.seam.api.async.SeamExecutorManager;
import me.novoro.seam.api.async.ServerScheduler;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.LocationUtil;
import me.novoro.seam.utils.RandomUtil;
import me.novoro.seam.utils.SeamLogger;
import me.novoro.seam.utils.randomteleport.RTPSettings;
import me.novoro.seam.utils.randomteleport.RTPWorldSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.novoro.seam.Seam.getServer;

public class RTPCommand extends CommandBase {
    private static final Set<UUID> queued = Collections.synchronizedSet(new HashSet<>());
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public RTPCommand() {
        super("randomteleport", "seam.rtp", 2, "rtp");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    RTPWorldSettings settings = RTPSettings.getWorldSettings(player.getServerWorld());
                    if (settings == null) {
                        LangManager.sendLang(context.getSource(), "RTP-World-Blacklisted");
                        return 0;
                    }
                    return handleRTP(player, settings);
                })
                .then(argument("world", StringArgumentType.string())
                        .requires(source -> this.permission(source, "seam.rtp.specific", 2))
                        .suggests((context, builder) -> CommandSource.suggestMatching(RTPSettings.getNonRedirectedWorlds(), builder))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String worldName = StringArgumentType.getString(context, "world");
                            RTPWorldSettings settings = RTPSettings.getWorldSettings(worldName);
                            if (settings == null) {
                                LangManager.sendLang(context.getSource(), "RTP-World-Blacklisted");
                                return 0;
                            }
                            return handleRTP(player, settings);
                        })
                );
    }

    private int handleRTP(ServerPlayerEntity player, RTPWorldSettings settings) {
        if (!this.permission(player, settings.getPermission(), 2)) {
            LangManager.sendLang(player, "RTP-No-Permission");
            return 0;
        }
        if (!queued.add(player.getUuid())) {
            LangManager.sendLang(player, "RTP-Already-Queued-Message");
            return 0;
        }

        long remaining = getCooldownRemaining(player.getUuid(), settings);
        if (remaining > 0) {
            queued.remove(player.getUuid());
            Map<String, String> replacements = Map.of("{cooldown}", String.valueOf(remaining));
            LangManager.sendLang(player, "RTP-Cooldown-Message", replacements);
            return 0;
        }

        LangManager.sendLang(player, "RTP-Queued-Message");

        MinecraftServer server = getServer();
        generateCandidatesAsync(settings)
                .thenCompose(candidates -> evaluateCandidatesSync(server, settings, candidates, 0))
                .whenComplete((location, err) -> ServerScheduler.runSync(server, () -> {
                    queued.remove(player.getUuid());
                    if (err != null) SeamLogger.warn("RTP failed for " + player.getName().getString() + ": " + err.getMessage());
                    if (player.isDisconnected()) return;

                    if (location != null) {
                        cooldowns.put(player.getUuid(), System.currentTimeMillis());
                        Map<String, String> replacements = new HashMap<>();
                        location.addReplacements(replacements);
                        location.teleport(player);
                        LangManager.sendLang(player, "RTP-Success-Message", replacements);
                    } else {
                        Map<String, String> replacements = Map.of("{attempts}", String.valueOf(RTPSettings.getMaxAttempts()));
                        LangManager.sendLang(player, "RTP-Location-Not-Found", replacements);
                    }
                }));

        return Command.SINGLE_SUCCESS;
    }

    private static long getCooldownRemaining(UUID playerId, RTPWorldSettings settings) {
        long cooldownMs = settings.getCooldown() * 1000L;
        if (cooldownMs <= 0) return 0;
        Long lastUse = cooldowns.get(playerId);
        if (lastUse == null) return 0;
        long elapsed = System.currentTimeMillis() - lastUse;
        long remaining = cooldownMs - elapsed;
        return remaining > 0 ? (remaining + 999) / 1000 : 0;
    }

    private static CompletableFuture<List<int[]>> generateCandidatesAsync(RTPWorldSettings settings) {
        return SeamExecutorManager.getDefaultExecutor().runAsync(() -> {
            int max = RTPSettings.getMaxAttempts();
            List<int[]> candidates = new ArrayList<>(max);
            for (int i = 0; i < max; i++) {
                int[] offset = settings.getRandomOffset();
                candidates.add(new int[]{
                        settings.getCenterX() + offset[0],
                        settings.getCenterZ() + offset[1]
                });
            }
            return candidates;
        });
    }

    private static CompletableFuture<Location> evaluateCandidatesSync(MinecraftServer server, RTPWorldSettings settings, List<int[]> candidates, int index) {
        if (index >= candidates.size()) return CompletableFuture.completedFuture(null);
        int[] c = candidates.get(index);
        return ServerScheduler.runSync(server, () -> checkCandidate(settings, c[0], c[1]))
                .thenCompose(candidate -> candidate != null
                        ? CompletableFuture.completedFuture(candidate)
                        : evaluateCandidatesSync(server, settings, candidates, index + 1));
    }

    private static Location checkCandidate(RTPWorldSettings settings, int x, int z) {
        ServerWorld world = settings.getWorld();
        if (world == null) return null;

        BlockPos.Mutable biomePos = new BlockPos.Mutable();
        biomePos.set(x, world.getSeaLevel(), z);
        Optional<RegistryKey<Biome>> biome = world.getBiome(biomePos).getKey();
        if (biome.isEmpty() || RTPSettings.isBiomeBlacklisted(biome.get().getValue())) return null;

        int startY = settings.isAllowCaveTeleports()
                ? RandomUtil.randomIntBetween(world.getBottomY(), settings.getHighestY())
                : settings.getHighestY();

        Location candidate = LocationUtil.findSafeRTPLocation(world, x, startY, z, settings.isAllowCaveTeleports());
        if (candidate == null) return null;

        biomePos.set(x, (int) candidate.getY(), z);
        Optional<RegistryKey<Biome>> landingBiome = world.getBiome(biomePos).getKey();
        if (landingBiome.isPresent() && RTPSettings.isBiomeBlacklisted(landingBiome.get().getValue())) return null;

        return candidate;
    }
}

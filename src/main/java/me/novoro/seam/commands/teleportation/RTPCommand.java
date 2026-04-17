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
import me.novoro.seam.utils.randomteleport.RTPSettings;
import me.novoro.seam.utils.randomteleport.RTPWorldSettings;
import net.minecraft.registry.RegistryKey;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.*;
import static me.novoro.seam.Seam.getServer;

public class RTPCommand extends CommandBase {
    private static final Set<UUID> queued = Collections.synchronizedSet(new HashSet<>());

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

        LangManager.sendLang(player, "RTP-Queued-Message");

        SeamExecutorManager.getDefaultExecutor().<Location>runAsync(() -> findLocation(settings))
                .whenComplete((location, err) -> ServerScheduler.runSync(getServer(), () -> {
                    queued.remove(player.getUuid());
                    if (player.isDisconnected()) return;

                    if (location != null) {
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

    private static Location findLocation(RTPWorldSettings settings) {
        ServerWorld world = settings.getWorld();
        if (world == null) return null;

        int max = RTPSettings.getMaxAttempts();
        for (int i = 0; i < max; i++) {
            int x = settings.getCenterX() + settings.getRandomIntInBounds();
            int z = settings.getCenterZ() + settings.getRandomIntInBounds();

            Optional<RegistryKey<Biome>> biome = world.getBiome(new BlockPos(x, world.getSeaLevel(), z)).getKey();
            if (biome.isEmpty() || RTPSettings.isBiomeBlacklisted(biome.get().getValue())) continue;

            int startY = settings.isAllowCaveTeleports()
                    ? RandomUtil.randomIntBetween(world.getBottomY(), settings.getHighestY())
                    : settings.getHighestY();

            Location candidate = LocationUtil.findSafeRTPLocation(world, x, startY, z, settings.isAllowCaveTeleports());
            if (candidate != null) return candidate;
        }
        return null;
    }
}
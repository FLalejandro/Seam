package me.novoro.seam.commands.utility;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Provides command to check when a player was last online.
 */
public class SeenCommand extends CommandBase {
    public SeenCommand() {
        super("seen", "seem.seen", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return argument("target", EntityArgumentType.player())
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "target");

                    if (profiles.isEmpty()) {
                        source.sendError(Text.of("No player specified!"));
                        return 0;
                    }

                    GameProfile profile = profiles.iterator().next();
                    handleSeenCommand(source, profile);
                    return Command.SINGLE_SUCCESS;
                });
    }

    private void handleSeenCommand(ServerCommandSource source, GameProfile profile) {
        MinecraftServer server = source.getServer();
        PlayerManager playerManager = server.getPlayerManager();
        UUID playerUuid = profile.getId();

        // 1. Check if player is currently online
        ServerPlayerEntity onlinePlayer = playerManager.getPlayer(playerUuid);
        if (onlinePlayer != null) {
            LangManager.sendLang(source, "Seen-Online",
                    Map.of("{player}", profile.getName()));
            return;
        }

        // 2. Check UserCache for last known access time
        long lastOnlineTime = getLastAccessTime(server, playerUuid);

        if (lastOnlineTime == 0) {
            // 3. Fallback to player data file
            lastOnlineTime = getLastPlayedTime(playerManager, playerUuid);
        }

        if (lastOnlineTime == 0) {
            // Player has never played before
            LangManager.sendLang(source, "Seen-Never-Played",
                    Map.of("{player}", profile.getName()));
            return;
        }

        // Calculate time since last online
        long currentTime = System.currentTimeMillis();
        long timeDiffSeconds = (currentTime - lastOnlineTime) / 1000;

        // Format and send message
        String formattedTime = TimeUtil.getFormattedTime(timeDiffSeconds);
        LangManager.sendLang(source, "Seen-Offline",
                Map.of("{player}", profile.getName(), "{time}", formattedTime));
    }

    /**
     * Get last access time from UserCache (faster)
     */
    private long getLastAccessTime(MinecraftServer server, UUID playerUuid) {
        UserCache userCache = server.getUserCache();
        if (userCache == null) return 0;

        Optional<GameProfile> cachedProfile = userCache.getByUuid(playerUuid);
        if (!cachedProfile.isPresent()) return 0;

        // Get expiration date from cache entry
        Date expiration = userCache.get(cachedProfile.get()).getExpirationDate();
        // Calculate last access time (cache entries expire after 1 month)
        return expiration.getTime() - TimeUnit.DAYS.toMillis(30);
    }
}

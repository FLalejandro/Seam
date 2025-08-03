package me.novoro.seam.commands.utility;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.Seam;
import me.novoro.seam.utils.TimeUtil;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class PlaytimeCommand extends CommandBase {
    public PlaytimeCommand() {
        super("playtime", "seam.playtime", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            UUID uuid = player.getUuid();
            long playtimeTicks = checkPlaytime(uuid);
            LangManager.sendLang(player, "PlayTime-Self", Map.of("{playtime}",
            TimeUtil.getFormattedTime(playtimeTicks / 20)));
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", GameProfileArgumentType.gameProfile())
                .requires(source -> this.permission(source, "seam.playtimeother", 4))
                .executes(context -> {
                    Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "target");
                    GameProfile targetProfile = profiles.iterator().next();
                    UUID targetUuid = targetProfile.getId();
                    String targetName = targetProfile.getName();
                    long playtimeTicks = checkPlaytime(targetUuid);
                    LangManager.sendLang(context.getSource().getPlayerOrThrow(), "PlayTime-Other", Map.of(
                            "{playtime}", TimeUtil.getFormattedTime(playtimeTicks / 20),
                            "{player}", targetName));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private static long checkPlaytime(UUID uuid) {
        MinecraftServer server = Seam.getServer();
        PlayerManager playerManager = server.getPlayerManager();
        ServerPlayerEntity player = playerManager.getPlayer(uuid);
        if (player != null) return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

        File statsDir = server.getSavePath(WorldSavePath.STATS).toFile();
        File statFile = new File(statsDir, uuid + ".json");
        if (!statFile.exists()) return 0;

        ServerStatHandler statHandler = new ServerStatHandler(server, statFile);
        return statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

    }
}
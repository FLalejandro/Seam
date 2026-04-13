package me.novoro.seam.commands.teleportation.homes;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.PlayerStorageManager;
import me.novoro.seam.objects.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.novoro.seam.Seam.getServer;

public class ListHomesCommand extends CommandBase {
    public ListHomesCommand() {
        super("listhomes", "seam.listhomes", 2, "homes");
    }
    
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            PlayerData data = PlayerStorageManager.get(player.getUuid());
            List<String> homeNames = data.getHomeNames();

            if (homeNames.isEmpty()) {
                LangManager.sendLang(player, "Home-List-Empty");
                return Command.SINGLE_SUCCESS;
            }
 
            String homeList = String.join(", ", homeNames);
            LangManager.sendLang(player, "Home-List", Map.of("{homes}", homeList));
            return Command.SINGLE_SUCCESS;
        }).then(argument("player", StringArgumentType.string())
                .requires(source -> this.permission(source, "seam.home.other", 2))
                .suggests((context, builder) -> {
                    getServer().getPlayerManager().getPlayerList().forEach(p -> builder.suggest(p.getName().getString()));
                    return CompletableFuture.completedFuture(builder.build());
                })
                .executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");

                    ServerPlayerEntity onlineTarget = getServer().getPlayerManager().getPlayer(playerName);
                    UUID targetUuid;
                    String targetDisplayName;

                    if (onlineTarget != null) {
                        targetUuid = onlineTarget.getUuid();
                        targetDisplayName = onlineTarget.getName().getString();
                    } else {
                        Optional<GameProfile> profile = getServer().getUserCache().findByName(playerName);
                        if (profile.isEmpty()) {
                            LangManager.sendLang(context.getSource(), "Invalid-Player", Map.of("{input}", playerName));
                            return 0;
                        }
                        targetUuid = profile.get().getId();
                        targetDisplayName = profile.get().getName();
                    }

                    PlayerData data = PlayerStorageManager.get(targetUuid);
                    List<String> homeNames = data.getHomeNames();

                    if (homeNames.isEmpty()) {
                        LangManager.sendLang(context.getSource(), "Home-Other-List-Empty",
                                Map.of("{player}", targetDisplayName));
                        return Command.SINGLE_SUCCESS;
                    }

                    String homeList = String.join(", ", homeNames);
                    LangManager.sendLang(context.getSource(), "Home-Other-List", Map.of(
                            "{player}", targetDisplayName,
                            "{homes}", homeList
                    ));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}

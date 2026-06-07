package me.novoro.seam.commands.teleportation.homes;
 
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.PlayerStorageManager;
import me.novoro.seam.objects.Home;
import me.novoro.seam.objects.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
 
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
 
import static me.novoro.seam.Seam.getServer;
 
public class HomeOtherCommand extends CommandBase {
    public HomeOtherCommand() {
        super("homeother", "seam.home.other", 2);
    }
 
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("player", StringArgumentType.string())
                .suggests((context, builder) -> {
                    getServer().getPlayerManager().getPlayerList()
                            .forEach(p -> builder.suggest(p.getName().getString()));
                    return CompletableFuture.completedFuture(builder.build());
                })
                .then(argument("home-name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            ServerPlayerEntity target = getServer().getPlayerManager().getPlayer(playerName);
                            if (target != null) {
                                PlayerStorageManager.get(target.getUuid()).getHomeNames().forEach(builder::suggest);
                            } else {
                                getServer().getUserCache().findByName(playerName).ifPresent(profile ->
                                        PlayerStorageManager.get(profile.getId()).getHomeNames().forEach(builder::suggest));
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        })
                        .executes(context -> {
                            ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
                            String playerName = StringArgumentType.getString(context, "player");
                            String homeName = StringArgumentType.getString(context, "home-name");
 
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
                            Home home = data.getHome(homeName);
 
                            if (home == null) {
                                LangManager.sendLang(sender, "Home-Invalid", Map.of("{home}", homeName));
                                return 0;
                            }
 
                            home.teleport(sender);
                            LangManager.sendLang(sender, "Home-Other-Teleport", Map.of(
                                    "{player}", targetDisplayName,
                                    "{home}", homeName
                            ));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}
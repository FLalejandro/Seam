package me.novoro.seam.commands.teleportation.homes;

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
import java.util.concurrent.CompletableFuture;

import static me.novoro.seam.Seam.getServer;

public class HomeCommand extends CommandBase {
    
    public HomeCommand() {
        super("home", "seam.home", 2);
    }
    
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("home-name", StringArgumentType.string())
                .suggests((context, builder) -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        PlayerStorageManager.get(player.getUuid()).getHomeNames().forEach(builder::suggest);
                    }
                    return CompletableFuture.completedFuture(builder.build());
                })
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String homeName = StringArgumentType.getString(context, "home-name");
                    PlayerData data = PlayerStorageManager.get(player.getUuid());
                    Home home = data.getHome(homeName);

                    if (home == null){
                        LangManager.sendLang(player, "Home-Invalid", Map.of("{home}", homeName));
                        return 0;
                    }

                    home.teleport(player);
                    LangManager.sendLang(player, "Home-Teleport", Map.of("{home}", homeName));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}

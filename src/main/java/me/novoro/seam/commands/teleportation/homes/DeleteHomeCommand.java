package me.novoro.seam.commands.teleportation.homes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.PlayerStorageManager;
import me.novoro.seam.objects.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
 
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DeleteHomeCommand extends CommandBase {
    public DeleteHomeCommand() {
        super("deletehome", "seam.deletehome", 2);
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
                    
                    if(!data.deleteHome(homeName)) {
                        LangManager.sendLang(player, "Home-Deleted-Invalid", Map.of("{home}", homeName));
                        return 0;
                    }

                    PlayerStorageManager.save(player.getUuid());
                    LangManager.sendLang(player, "Home-Deleted", Map.of("{home}", homeName));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
    
}

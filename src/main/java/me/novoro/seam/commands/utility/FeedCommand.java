package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Map;

/**
 * Provides command to feed the player.
 */
public class FeedCommand extends CommandBase {
    public FeedCommand() {
        super("feed", "seam.feed", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            feedPlayer(player);
            LangManager.sendLang(context.getSource(), "Feed-Self-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.feedtargets", 4))
                .executes(context -> {
                    List<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target").stream().toList();
                    for (ServerPlayerEntity player : players) {
                        feedPlayer(player);
                        LangManager.sendLang(player, "Feed-Self-Message");
                    }
                    if (players.size() == 1) {
                        String firstPlayer = players.getFirst().getName().getString();
                        LangManager.sendLang(context.getSource(), "Feed-Other-Message", Map.of("{player}", firstPlayer));
                    } else {
                        LangManager.sendLang(context.getSource(), "Feed-All-Message", Map.of("{amount}", String.valueOf(players.size())));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Feeds the target player.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int feedPlayer(ServerPlayerEntity ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getCommandSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        // Set hunger and saturation to max
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(20);

        return 1;
    }


}

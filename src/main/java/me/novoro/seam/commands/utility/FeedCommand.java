package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
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
            FeedCommand.feedPlayer(context.getSource().getPlayerOrThrow());
            LangManager.sendLang(context.getSource(), "Feed-Self-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.feedtargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    // Loop directly over the collection:
                    players.forEach(player -> {
                        feedPlayer(player);
                        LangManager.sendLang(player, "Feed-Self-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "Feed-Other-Message", Map.of("{player}", firstPlayer.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "Feed-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Feeds the target players.
     *
     * @param targets The target players.
     */
    private static void feedPlayer(ServerPlayerEntity... targets) {
        // Set Hunger and Saturation to max
        for (ServerPlayerEntity target : targets) {
            target.getHungerManager().setFoodLevel(20);
            target.getHungerManager().setSaturationLevel(20);
        }
    }


}

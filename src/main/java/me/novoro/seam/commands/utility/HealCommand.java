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
 * Provides command to heal the player.
 */
public class HealCommand extends CommandBase {
    public HealCommand() {
        super("heal", "seam.heal", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            healPlayer(player);
            LangManager.sendLang(context.getSource(), "Heal-Self-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.healtargets", 4))
                .executes(context -> {
                    List<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target").stream().toList();
                    for (ServerPlayerEntity player : players) {
                        healPlayer(player);
                        LangManager.sendLang(player, "Heal-Self-Message");
                    }
                    if (players.size() == 1) {
                        String firstPlayer = players.getFirst().getName().getString();
                        LangManager.sendLang(context.getSource(), "Heal-Other-Message", Map.of("{player}", firstPlayer));
                    } else {
                        LangManager.sendLang(context.getSource(), "Heal-All-Message", Map.of("{amount}", String.valueOf(players.size())));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Heals the target player.
     *
     * @param ctx The command context.
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static int healPlayer(ServerPlayerEntity ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getCommandSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return 0;

        // Set health to max
        player.setHealth(player.getMaxHealth());

        return 1;
    }


}




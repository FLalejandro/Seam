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
 * Provides command to heal the player.
 */
public class HealCommand extends CommandBase {
    public HealCommand() {
        super("heal", "seam.heal", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            HealCommand.healPlayer(context.getSource().getPlayerOrThrow());
            LangManager.sendLang(context.getSource(), "Heal-Self-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.healtargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        healPlayer(player);
                        LangManager.sendLang(player, "Heal-Self-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "Heal-Other-Message", Map.of("{player}", firstPlayer.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "Heal-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Heals the target player.
     *
     * @param targets The target players.
     * @return 1 if successful, 0 otherwise.
     */
    private static void healPlayer(ServerPlayerEntity... targets) {
        // Set Health to max
        for (ServerPlayerEntity target : targets) {
            target.setHealth(target.getMaxHealth());
        }
    }

}




// ToDo: Hook this up w/ player storage

package me.novoro.seam.commands.ability;

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
 * Provides command to toggle flight for players.
 */
public class FlyCommand extends CommandBase {
    public FlyCommand() {
        super("fly", "seam.fly", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            boolean wasEnabled = toggleFly(player);
            LangManager.sendLang(context.getSource(), wasEnabled ? "Fly-Disabled-Message" : "Fly-Enabled-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.flytargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        boolean wasEnabled = toggleFly(player);
                        LangManager.sendLang(player, wasEnabled ? "Fly-Disabled-Message" : "Fly-Enabled-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity first = players.iterator().next();
                        String messageKey = first.getAbilities().allowFlying ? "Fly-Other-Enabled-Message" : "Fly-Other-Disabled-Message";
                        LangManager.sendLang(context.getSource(), messageKey, Map.of("{player}", first.getName().getString()));
                    } else {
                        LangManager.sendLang(context.getSource(), "Fly-All-Message", Map.of("{amount}", String.valueOf(players.size())));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Toggles flight on the target player.
     *
     * @param target the target players.
     * @return true if flight is enabled after toggling, false otherwise.
     */
    // ToDo: Move toggles to player data
    private static boolean toggleFly(ServerPlayerEntity target) {
        boolean wasAllowed = target.getAbilities().allowFlying;
        target.getAbilities().allowFlying = !wasAllowed;

        // Disable flying state if flight is being turned off
        if (wasAllowed) {
            target.getAbilities().flying = false;
        }

        target.sendAbilitiesUpdate();
        return wasAllowed;
    }
}

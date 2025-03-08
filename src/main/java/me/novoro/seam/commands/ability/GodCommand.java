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

public class GodCommand extends CommandBase {
    public GodCommand() {
        super("god", "seam.god", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            boolean enabled = toggleGod(player);
            if (enabled) LangManager.sendLang(context.getSource(), "God-Enabled-Message");
            else LangManager.sendLang(context.getSource(), "God-Disabled-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.godtargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        boolean enabled = toggleGod(player);
                        if (enabled) LangManager.sendLang(player, "God-Enabled-Message");
                        else LangManager.sendLang(player, "God-Disabled-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity first = players.iterator().next();
                        if (first.getAbilities().invulnerable) LangManager.sendLang(context.getSource(), "God-Other-Enabled-Message", Map.of("{player}", first.getName().getString()));
                        else LangManager.sendLang(context.getSource(), "God-Other-Disabled-Message", Map.of("{player}", first.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "God-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Toggles God mode for the target player.
     *
     * @param targets the target players.
     * @return true if god mode is enabled after toggling, false otherwise.
     */
    private static boolean toggleGod(ServerPlayerEntity... targets) {
        for (ServerPlayerEntity target : targets) {
            boolean wasGod = target.getAbilities().invulnerable;
            target.getAbilities().invulnerable = !wasGod;
            target.sendAbilitiesUpdate();
            return !wasGod;
        }
        return false;
    }
}

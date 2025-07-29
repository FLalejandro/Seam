// ToDo: Hook this up w/ player storage

package me.novoro.seam.commands.ability;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

/**
 * Provides command to toggle night vision for players.
 */
public class NightVisionCommand extends CommandBase {
    public NightVisionCommand() {
        super("nightvision", "seam.nightvision", 2, "nv");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            boolean wasEnabled = toggleNightVision(player);
            LangManager.sendLang(player, wasEnabled ? "NightVision-Disabled-Message" : "NightVision-Enabled-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.nightvisiontargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        boolean wasEnabled = toggleNightVision(player);
                        LangManager.sendLang(player, wasEnabled ? "NightVision-Disabled-Message" : "NightVision-Enabled-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity first = players.iterator().next();
                        if (first.hasStatusEffect(StatusEffects.NIGHT_VISION)) LangManager.sendLang(context.getSource(), "NightVision-Other-Enabled-Message", Map.of("{player}", first.getName().getString()));
                        else LangManager.sendLang(context.getSource(), "NightVision-Other-Disabled-Message", Map.of("{player}", first.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "NightVision-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Toggles the night vision effect on the target player.
     *
     * @param target the target players.
     * @return true if night vision is enabled after toggling, false otherwise.
     */
    // ToDo: Move toggles to player data
    private static boolean toggleNightVision(ServerPlayerEntity target) {
        boolean hadEffect = target.hasStatusEffect(StatusEffects.NIGHT_VISION);
        if (hadEffect) target.removeStatusEffect(StatusEffects.NIGHT_VISION);
        else target.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, false, false, true));
        return hadEffect;
    }
}

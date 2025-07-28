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

public class WaterBreathingCommand extends CommandBase {
    public WaterBreathingCommand(){
        super("waterbreathing", "seam.waterbreathing", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            boolean wasEnabled = toggleWaterBreathing(player);
            LangManager.sendLang(player, wasEnabled ? "WaterBreathing-Disabled-Message" : "WaterBreathing-Enabled-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.waterbreathingtargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        boolean wasEnabled = toggleWaterBreathing(player);
                        LangManager.sendLang(player, wasEnabled ? "WaterBreathing-Disabled-Message" : "WaterBreathing-Enabled-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity first = players.iterator().next();
                        if (first.hasStatusEffect(StatusEffects.WATER_BREATHING)) LangManager.sendLang(context.getSource(), "WaterBreathing-Other-Enabled-Message", Map.of("{player}", first.getName().getString()));
                        else LangManager.sendLang(context.getSource(), "WaterBreathing-Other-Disabled-Message", Map.of("{player}", first.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "WaterBreathing-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Toggles the water breathing effect on the target player.
     *
     * @param target the target players.
     * @return true if water breathing is enabled after toggling, false otherwise.
     */
    // ToDo: Move toggles to player data
    private static boolean toggleWaterBreathing(ServerPlayerEntity target) {
        boolean hadEffect = target.hasStatusEffect(StatusEffects.WATER_BREATHING);
        if (hadEffect) target.removeStatusEffect(StatusEffects.WATER_BREATHING);
        else target.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, StatusEffectInstance.INFINITE, 0, false, false, true));
        return hadEffect;
    }
}

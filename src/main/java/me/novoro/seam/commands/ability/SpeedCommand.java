package me.novoro.seam.commands.ability;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

import java.util.Collection;
import java.util.Map;

/**
 * Provides command to modify a player's walk or fly speed.
 * Usage: /speed <walk|fly> <value> [target]
 *        /speed reset [target]
 */
public class SpeedCommand extends CommandBase {
    private static final double DEFAULT_WALK_SPEED = 0.1;
    private static final float DEFAULT_FLY_SPEED = 0.05f;

    public SpeedCommand() {
        super("speed", "seam.speed", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command
                .then(literal("walk").then(argument("value", FloatArgumentType.floatArg(0.1f, 10))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            float value = FloatArgumentType.getFloat(context, "value");
                            setWalkSpeed(player, value);
                            LangManager.sendLang(context.getSource(), "Speed-Walk-Set-Message", Map.of("{speed}", String.valueOf(value)));
                            return Command.SINGLE_SUCCESS;
                        }).then(argument("target", EntityArgumentType.players())
                                .requires(source -> this.permission(source, "seam.speedtargets", 4))
                                .executes(context -> {
                                    float value = FloatArgumentType.getFloat(context, "value");
                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                                    players.forEach(player -> {
                                        setWalkSpeed(player, value);
                                        LangManager.sendLang(player, "Speed-Walk-Set-Message", Map.of("{speed}", String.valueOf(value)));
                                    });
                                    if (players.size() == 1) {
                                        ServerPlayerEntity first = players.iterator().next();
                                        LangManager.sendLang(context.getSource(), "Speed-Walk-Set-Other-Message", Map.of("{player}", first.getName().getString(), "{speed}", String.valueOf(value)));
                                    } else LangManager.sendLang(context.getSource(), "Speed-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                ))
                .then(literal("fly").then(argument("value", FloatArgumentType.floatArg(0.1f, 10))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            float value = FloatArgumentType.getFloat(context, "value");
                            setFlySpeed(player, value);
                            LangManager.sendLang(context.getSource(), "Speed-Fly-Set-Message", Map.of("{speed}", String.valueOf(value)));
                            return Command.SINGLE_SUCCESS;
                        }).then(argument("target", EntityArgumentType.players())
                                .requires(source -> this.permission(source, "seam.speedtargets", 4))
                                .executes(context -> {
                                    float value = FloatArgumentType.getFloat(context, "value");
                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                                    players.forEach(player -> {
                                        setFlySpeed(player, value);
                                        LangManager.sendLang(player, "Speed-Fly-Set-Message", Map.of("{speed}", String.valueOf(value)));
                                    });
                                    if (players.size() == 1) {
                                        ServerPlayerEntity first = players.iterator().next();
                                        LangManager.sendLang(context.getSource(), "Speed-Fly-Set-Other-Message", Map.of("{player}", first.getName().getString(), "{speed}", String.valueOf(value)));
                                    } else LangManager.sendLang(context.getSource(), "Speed-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                ))
                .then(literal("reset").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    resetSpeed(player);
                    LangManager.sendLang(context.getSource(), "Speed-Reset-Message");
                    return Command.SINGLE_SUCCESS;
                }).then(argument("target", EntityArgumentType.players())
                        .requires(source -> this.permission(source, "seam.speedtargets", 4))
                        .executes(context -> {
                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                            players.forEach(player -> {
                                resetSpeed(player);
                                LangManager.sendLang(player, "Speed-Reset-Message");
                            });
                            if (players.size() == 1) {
                                ServerPlayerEntity first = players.iterator().next();
                                LangManager.sendLang(context.getSource(), "Speed-Reset-Other-Message", Map.of("{player}", first.getName().getString()));
                            } else LangManager.sendLang(context.getSource(), "Speed-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                            return Command.SINGLE_SUCCESS;
                        })
                ));
    }

    /**
     * Sets the walk speed for the target player by directly setting the base value of Generic Movement Speed
     * When testing with .getAbilities().setWalkSpeed, all it did was increase FOV without affecting movement speed. Maybe this gets
     * patched in a future MC update
     *
     * @param target the target player.
     * @param value the speed multiplier (0.1-10). 1 = normal, 2 = double, 0.5 = half.
     */
    private static void setWalkSpeed(ServerPlayerEntity target, float value) {
        EntityAttributeInstance attribute = target.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attribute == null) return;
        attribute.setBaseValue(value * 0.1);
    }

    /**
     * Sets the fly speed for the target player.
     *
     * @param target the target player.
     * @param value the speed value (0-10).
     */
    private static void setFlySpeed(ServerPlayerEntity target, float value) {
        target.getAbilities().setFlySpeed(value / 20.0f);
        target.sendAbilitiesUpdate();
    }

    /**
     * Resets both walk and fly speed to their default values.
     *
     * @param target the target player.
     */
    private static void resetSpeed(ServerPlayerEntity target) {
        EntityAttributeInstance attribute = target.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attribute != null) attribute.setBaseValue(DEFAULT_WALK_SPEED);
        target.getAbilities().setFlySpeed(DEFAULT_FLY_SPEED);
        target.sendAbilitiesUpdate();
    }
}

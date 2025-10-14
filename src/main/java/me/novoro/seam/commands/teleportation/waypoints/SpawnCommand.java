package me.novoro.seam.commands.teleportation.waypoints;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.WaypointManager;
import me.novoro.seam.objects.Waypoint;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

//TODO: Need to suggest spawns
//TODO: Target spawns is wonky. `/spawn <spawn> <target>` sends target to their spawn, not specified spawn.
//TODO: Players **ARE** correctly being sent to highest priority spawn
public class SpawnCommand extends CommandBase {
    public SpawnCommand() {
        super("spawn", "seam.spawn", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            Waypoint spawn = WaypointManager.determineSpawnPoint(player);

            if (spawn == null) {
                LangManager.sendLang(player, "Spawn-Invalid-Point");
                return 0;
            }

            spawn.teleport(player);
            LangManager.sendLang(player, "Spawn-Teleport");

            return Command.SINGLE_SUCCESS;
        }).then(argument("spawn-name", StringArgumentType.string())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String spawnName = StringArgumentType.getString(context, "spawn-name");
                    Waypoint spawn = WaypointManager.getSpawn(spawnName);

                    if (spawn == null) {
                        LangManager.sendLang(player, "Spawn-Not-Found", Map.of("{spawn}", spawnName));
                        return 0;
                    }

                    // TODO: Permission Check

                    // Teleport to specific spawn
                    spawn.teleport(player);
                    LangManager.sendLang(player, "Spawn-Teleport", Map.of("{spawn}", spawnName));

                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("target", EntityArgumentType.players())
                        .requires(source -> this.permission(source, "seam.spawn.other", 3))
                        .executes(context -> {
                            Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "target");

                            int teleported = 0;
                            for (ServerPlayerEntity target : targets) {
                                Waypoint spawn = WaypointManager.determineSpawnPoint(target);
                                if (spawn == null) {
                                    LangManager.sendLang(context.getSource(), "Spawn-Invalid-Point-Other",
                                            Map.of("{player}", target.getName().getString()));
                                    continue;
                                }

                                spawn.teleport(target);
                                teleported++;
                                LangManager.sendLang(target, "Spawn-Force-Teleport");
                            }

                            if (targets.size() == 1) {
                                ServerPlayerEntity first = targets.iterator().next();
                                LangManager.sendLang(context.getSource(), "Spawn-Force-Admin",
                                        Map.of("{player}", first.getName().getString()));
                            } else {
                                LangManager.sendLang(context.getSource(), "Spawn-All-Admin",
                                        Map.of("{amount}", String.valueOf(teleported)));
                            }

                            return Command.SINGLE_SUCCESS;
                        })).then(argument("spawn-name", StringArgumentType.string())
                        .executes(context -> {
                            Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "target");
                            String spawnName = StringArgumentType.getString(context, "spawn-name");
                            Waypoint spawn = WaypointManager.getSpawn(spawnName);

                            if (spawn == null) {
                                LangManager.sendLang(context.getSource(), "Spawn-Not-Found",
                                        Map.of("{spawn}", spawnName));
                                return 0;
                            }

                            boolean all = targets.size() > 1;
                            if (all && !this.permission(context.getSource(), "seam.spawn.all", 4)) {
                                LangManager.sendLang(context.getSource(), "No-Permission");
                                return 0;
                            }

                            int teleported = 0;
                            for (ServerPlayerEntity target : targets) {
                                spawn.teleport(target);
                                teleported++;
                                LangManager.sendLang(target, "Spawn-Force-Teleport-Named",
                                        Map.of("{spawn}", spawnName));
                            }

                            if (all) {
                                LangManager.sendLang(context.getSource(), "Spawn-All-Named-Admin",
                                        Map.of("{amount}", String.valueOf(teleported),
                                                "{spawn}", spawnName));
                            } else {
                                ServerPlayerEntity first = targets.iterator().next();
                                LangManager.sendLang(context.getSource(), "Spawn-Force-Named-Admin",
                                        Map.of("{player}", first.getName().getString(),
                                                "{spawn}", spawnName));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

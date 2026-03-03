package me.novoro.seam.commands.teleportation.waypoints;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.WaypointManager;
import me.novoro.seam.objects.Waypoint;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static me.novoro.seam.Seam.getServer;

public class SpawnCommand extends CommandBase {
    public SpawnCommand() {
        super("spawn", "seam.spawn", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) {
                LangManager.sendLang(context.getSource(), "Player-Only-Command");
                return 0;
            }

            Waypoint spawn = WaypointManager.determineSpawnPoint(player);
            if (spawn == null) {
                LangManager.sendLang(context.getSource(), "Spawn-None-Message");
                return 0;
            }

            spawn.teleport(player);
            LangManager.sendLang(context.getSource(), "Spawn-Teleport");
            return Command.SINGLE_SUCCESS;
        }).then(argument("player", StringArgumentType.string())
                .requires(source -> this.permission(source, "seam.spawn.other", 3))
                .suggests((context, builder) -> {
                    getServer().getPlayerManager().getPlayerList().forEach(player -> builder.suggest(player.getName().getString()));
                    if (this.permission(context.getSource(), "seam.spawn.all", 4)) builder.suggest("all");
                    return CompletableFuture.completedFuture(builder.build());
                })
                .executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");

                    if (playerName.equalsIgnoreCase("all")) {
                        if (!this.permission(context.getSource(), "seam.spawn.all", 4)) {
                            LangManager.sendLang(context.getSource(), "Spawn-Invalid-Permission-All");
                            return 0;
                        }

                        int amount = 0;
                        int succeeded = 0;
                        for (ServerPlayerEntity target : getServer().getPlayerManager().getPlayerList()) {
                            amount++;
                            Waypoint spawn = WaypointManager.determineSpawnPoint(target);
                            if (spawn == null) continue;
                            spawn.teleport(target);
                            LangManager.sendLang(target, "Spawn-Force-Message-Player");
                            succeeded++;
                        }
                        LangManager.sendLang(context.getSource(), "Spawn-Teleported-All-Admin",
                                Map.of("{succeeded}", String.valueOf(succeeded), "{amount}", String.valueOf(amount)));
                        return Command.SINGLE_SUCCESS;
                    }

                    ServerPlayerEntity target = getServer().getPlayerManager().getPlayer(playerName);
                    if (target == null) {
                        LangManager.sendLang(context.getSource(), "Invalid-Player", Map.of("{input}", playerName));
                        return 0;
                    }

                    Waypoint spawn = WaypointManager.determineSpawnPoint(target);
                    if (spawn == null) {
                        LangManager.sendLang(context.getSource(), "Spawn-None-Message-Admin",
                                Map.of("{player}", target.getName().getString()));
                        return 0;
                    }

                    spawn.teleport(target);
                    LangManager.sendLang(target, "Spawn-Force-Message-Player");
                    LangManager.sendLang(context.getSource(), "Spawn-Force-Message-Admin",
                            Map.of("{player}", target.getName().getString()));
                    return Command.SINGLE_SUCCESS;
                }).then(argument("spawn-name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            WaypointManager.getAllSpawnNames().forEach(builder::suggest);
                            return CompletableFuture.completedFuture(builder.build());
                        })
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            String spawnName = StringArgumentType.getString(context, "spawn-name");
                            Waypoint spawn = WaypointManager.getSpawn(spawnName);

                            if (spawn == null) {
                                LangManager.sendLang(context.getSource(), "Spawn-Invalid-Point", Map.of("{input}", spawnName));
                                return 0;
                            }

                            if (playerName.equalsIgnoreCase("all")) {
                                if (!this.permission(context.getSource(), "seam.spawn.all", 4)) {
                                    LangManager.sendLang(context.getSource(), "Spawn-Invalid-Permission-All");
                                    return 0;
                                }

                                int amount = 0;
                                for (ServerPlayerEntity target : getServer().getPlayerManager().getPlayerList()) {
                                    spawn.teleport(target);
                                    LangManager.sendLang(target, "Spawn-Force-Message-Player");
                                    amount++;
                                }
                                LangManager.sendLang(context.getSource(), "Spawn-Teleported-All-Specific-Admin",
                                        Map.of("{amount}", String.valueOf(amount), "{spawn}", spawnName));
                                return Command.SINGLE_SUCCESS;
                            }

                            ServerPlayerEntity target = getServer().getPlayerManager().getPlayer(playerName);
                            if (target == null) {
                                LangManager.sendLang(context.getSource(), "Invalid-Player", Map.of("{input}", playerName));
                                return 0;
                            }

                            spawn.teleport(target);
                            LangManager.sendLang(target, "Spawn-Force-Message-Player");
                            LangManager.sendLang(context.getSource(), "Spawn-Force-Specific-Admin",
                                    Map.of("{player}", target.getName().getString(), "{spawn}", spawnName));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

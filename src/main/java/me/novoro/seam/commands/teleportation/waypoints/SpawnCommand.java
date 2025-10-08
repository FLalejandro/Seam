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

            // Teleport to spawn
            spawn.teleport(player);
            LangManager.sendLang(player, "Spawn-Teleport");

            return Command.SINGLE_SUCCESS;
        }).then(
                argument("spawn-name", StringArgumentType.string())
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
        );
    }
}

package me.novoro.seam.commands.teleportation.waypoints;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.WaypointManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class ListSpawnsCommand extends CommandBase {
    public ListSpawnsCommand() {
        super("listspawns", "seam.listspawns", 2, "spawns");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            StringBuilder spawnList = new StringBuilder();

            WaypointManager.getAllSpawnNames().forEach(name -> {
                var waypoint = WaypointManager.getSpawn(name);
                if (waypoint != null) {
                    spawnList.append(name)
                            .append(", ");
                }
            });

            if (!spawnList.isEmpty()) {
                spawnList.setLength(spawnList.length() - 2);
                LangManager.sendLang(player, "Spawn-List", Map.of("{spawns}", spawnList.toString()));
            } else {
                LangManager.sendLang(player, "Spawn-List-Empty");
            }

            return Command.SINGLE_SUCCESS;
        });
    }
}

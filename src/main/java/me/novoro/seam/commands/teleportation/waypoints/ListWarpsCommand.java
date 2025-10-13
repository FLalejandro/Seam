package me.novoro.seam.commands.teleportation.waypoints;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.WaypointManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class ListWarpsCommand extends CommandBase {
    public ListWarpsCommand() {
        super("listwarps", "seam.listwarps", 2, "warps");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            StringBuilder warpList = new StringBuilder();

            WaypointManager.getAllWarpNames().forEach(name -> {
                var waypoint = WaypointManager.getWarp(name);
                if (waypoint != null) {
                    warpList.append(name)
                            .append(", ");
                }
            });

            if (!warpList.isEmpty()) {
                warpList.setLength(warpList.length() - 2);
                LangManager.sendLang(player, "Warp-List", Map.of("{warps}", warpList.toString()));
            } else {
                LangManager.sendLang(player, "Warp-List-Empty");
            }

            return Command.SINGLE_SUCCESS;
        });
    }
}

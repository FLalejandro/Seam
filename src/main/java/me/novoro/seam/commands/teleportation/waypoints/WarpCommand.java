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

public class WarpCommand extends CommandBase {
    public WarpCommand() {
        super("warp", "seam.warp", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(
                argument("warp-name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            WaypointManager.getAllWarpNames().forEach(builder::suggest);
                            return CompletableFuture.completedFuture(builder.build());
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String warpName = StringArgumentType.getString(context, "warp-name");
                            Waypoint warp = WaypointManager.getWarp(warpName);

                            if (warp == null) {
                                LangManager.sendLang(player, "Warp-Invalid-Point", Map.of("{warp}", warpName));
                                return 0;
                            }

                            // TODO: Permission Check

                            warp.teleport(player);
                            LangManager.sendLang(player, "Warp-Teleport", Map.of("{warp}", warpName));

                            return Command.SINGLE_SUCCESS;
                        })
        );
    }
}

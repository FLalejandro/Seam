package me.novoro.seam.commands.teleportation.waypoints;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.WaypointManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DeleteWarpCommand extends CommandBase {
    public DeleteWarpCommand() {
        super("deletewarp", "seam.deletewarp", 2, "delwarp");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("warp-point", StringArgumentType.string())
                .suggests((context, builder) -> {
                    WaypointManager.getAllWarpNames().forEach(builder::suggest);
                    return CompletableFuture.completedFuture(builder.build());
                })
                .executes(context -> {
                    String warpName = context.getArgument("warp-point", String.class);
                    if (!WaypointManager.hasWarp(warpName)) {
                        LangManager.sendLang(context.getSource(),"Warp-Invalid-Point", Map.of("{input}", warpName));
                        return Command.SINGLE_SUCCESS;
                    }
                    WaypointManager.deleteWarp(warpName);
                    LangManager.sendLang(context.getSource(),"Warp-Deleted", Map.of("{warp}", warpName));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}

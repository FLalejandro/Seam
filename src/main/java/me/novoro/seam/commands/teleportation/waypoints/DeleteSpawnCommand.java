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

public class DeleteSpawnCommand extends CommandBase {
    public DeleteSpawnCommand() {
        super("deletespawn", "seam.deletespawn", 2, "delspawn");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("spawn-point", StringArgumentType.string())
                .suggests((context, builder) -> {
                    WaypointManager.getAllSpawnNames().forEach(builder::suggest);
                    return CompletableFuture.completedFuture(builder.build());
                })
                .executes(context -> {
                    String spawnName = context.getArgument("spawn-point", String.class);
                    if (!WaypointManager.hasSpawn(spawnName)) {
                        LangManager.sendLang(context.getSource(),"Spawn-Invalid-Point", Map.of("{input}", spawnName));
                        return Command.SINGLE_SUCCESS;
                    }
                    WaypointManager.deleteSpawn(spawnName);
                    LangManager.sendLang(context.getSource(),"Spawn-Deleted", Map.of("{spawn}", spawnName));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}

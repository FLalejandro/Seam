package me.novoro.seam.commands.teleportation.waypoints;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.WaypointManager;
import me.novoro.seam.utils.ColorUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class SetSpawnCommand extends CommandBase {
    public SetSpawnCommand() {
        super("setspawn", "seam.setspawn", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("spawn-point", StringArgumentType.string())
                .executes(context -> this.createSpawn(context.getSource(),
                        context.getArgument("spawn-point", String.class), 0))
                .then(argument("weight", IntegerArgumentType.integer())
                        .executes(context -> this.createSpawn(context.getSource(),
                                context.getArgument("spawn-point", String.class),
                                context.getArgument("weight", Integer.class)))));
    }

    private int createSpawn(ServerCommandSource source, String spawnName, int weight) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendMessage(ColorUtil.parseColour("&cThis command can only be used by a player!"));
            return 1;
        }

        if (WaypointManager.getSpawn(spawnName) != null) {
            LangManager.sendLang(source, "Spawn-Already-Exists", Map.of("{input}", spawnName));
            return 1;
        }

        String permission = "seam.spawn." + spawnName.toLowerCase();
        WaypointManager.createSpawn(player, spawnName, weight, permission);

        LangManager.sendLang(player, "Spawn-Set", Map.of(
                "{spawn}", spawnName,
                "{weight}", String.valueOf(weight),
                "{permission}", permission
        ));
        return 1;
    }
}


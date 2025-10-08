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

public class SetWarpCommand extends CommandBase {
    public SetWarpCommand() {
        super("setwarp", "seam.setwarp", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("warp-point", StringArgumentType.string())
                .executes(context -> this.createWarp(context.getSource(),
                        context.getArgument("warp-point", String.class)))
                .then(argument("permission", IntegerArgumentType.integer())
                        .executes(context -> this.createWarp(context.getSource(),
                                context.getArgument("warp-point", String.class)))));
    }

    private int createWarp(ServerCommandSource source, String warpName) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendMessage(ColorUtil.parseColour("&cThis command can only be used by a player!"));
            return 1;
        }

        if (WaypointManager.getWarp(warpName) != null) {
            LangManager.sendLang(source, "Warp-Already-Exists", Map.of("{input}", warpName));
            return 1;
        }

        String permission = "seam.warp." + warpName.toLowerCase();
        WaypointManager.createWarp(player, warpName, 0, permission);

        LangManager.sendLang(player, "Warp-Set", Map.of(
                "{warp}", warpName,
                "{permission}", permission
        ));
        return 1;
    }
}

package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.TPAUtil;
import me.novoro.seam.api.Location;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

import static me.novoro.seam.utils.TPAUtil.TeleportType.TPA;

public class TPACommand extends CommandBase {
    public TPACommand() { super("tpa", "seam.tpa", 2); }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("target", EntityArgumentType.players())
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    if(context.getSource().getPlayerOrThrow() == target) {
                        LangManager.sendLang(context.getSource(), "TP-Self-Error");
                        return Command.SINGLE_SUCCESS;
                    }
                    Location targetLocation = new Location(target);
                    TPAUtil.createTeleportRequest(context.getSource().getPlayerOrThrow(), target, targetLocation, TPA);
                    LangManager.sendLang(context.getSource().getPlayerOrThrow(), "TPA-Request-Send", Map.of("{player}", target.getName().getString()));
                    LangManager.sendLang(target, "TPA-Request-Receive", Map.of("{player}", context.getSource().getPlayerOrThrow().getName().getString()));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
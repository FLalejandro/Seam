package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.api.Location;
import me.novoro.seam.utils.TPAUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

import static me.novoro.seam.utils.TPAUtil.TeleportType.TPAHERE;

public class TPAHereCommand extends CommandBase {
    public TPAHereCommand() { super("tpahere", "seam.tpahere", 2); }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("target", EntityArgumentType.players())
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    if(context.getSource().getPlayerOrThrow() == target) {
                        LangManager.sendLang(context.getSource(), "TP-Self-Error");
                        return Command.SINGLE_SUCCESS;
                    }
                    Location senderLocation = new Location(context.getSource().getPlayerOrThrow());
                    TPAUtil.createTeleportRequest(context.getSource().getPlayerOrThrow(), target, senderLocation, TPAHERE);
                    LangManager.sendLang(context.getSource().getPlayerOrThrow(), "TPAHere-Request-Send", Map.of("{player}", target.getName().getString()));
                    LangManager.sendLang(target, "TPAHere-Request-Receive", Map.of("{player}", context.getSource().getPlayerOrThrow().getName().getString()));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
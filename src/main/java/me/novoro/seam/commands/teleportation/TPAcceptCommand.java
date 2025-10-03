package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.utils.TPAUtil;
import net.minecraft.server.command.ServerCommandSource;

public class TPAcceptCommand extends CommandBase {
    public TPAcceptCommand() { super("tpaccept", "seam.tpaccept", 2); }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            TPAUtil.handleTeleportRequest(context.getSource().getPlayerOrThrow(), true);
            return Command.SINGLE_SUCCESS;
        });
    }
}
package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.TPAUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TPACancelCommand extends CommandBase {
    public TPACancelCommand() {
        super("tpacancel", "seam.tpacancel", 2);
    }
    
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (!TPAUtil.cancelTeleportRequest(player.getUuid(), context.getSource().getServer())) {
                LangManager.sendLang(context.getSource(), "TPA-Cancel-None");
            }
            return Command.SINGLE_SUCCESS;
        });
    }
    
}

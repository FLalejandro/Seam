package me.novoro.seam.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.Seam;
import me.novoro.seam.utils.ColorUtil;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Seam's reload command.
 */
public final class SeamReloadCommand extends CommandBase {
    public SeamReloadCommand() {
        super("seam", "seam.reload", 4);
    }

    @Override
    public boolean bypassCommandCheck() {
        return true;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(literal("reload")
                .executes(context -> {
                    Seam.inst().reloadConfigs();
                    context.getSource().sendMessage(ColorUtil.parseColour(Seam.MOD_PREFIX + "&aReloaded Configs!"));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}

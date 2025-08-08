package me.novoro.seam.commands.fun;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Provides command to commit suicide.
 */
public class SuicideCommand extends CommandBase {
    public SuicideCommand() {
        super("suicide", "seam.suicide", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            context.getSource().getPlayerOrThrow().kill();
            LangManager.sendLang(context.getSource(), "Suicide-Commit");
            return Command.SINGLE_SUCCESS;
        });
    }
}
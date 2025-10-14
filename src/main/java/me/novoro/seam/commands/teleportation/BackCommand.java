package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import net.minecraft.server.command.ServerCommandSource;

//TODO: Optional Back on Death
public class BackCommand extends CommandBase {
    public BackCommand() {
        super("back", "seam.back", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return null;
    }
}

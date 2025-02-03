package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TopCommand extends CommandBase {
    public TopCommand() {
        super("top", "seam.top", 1);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            this.top(context.getSource(), context.getSource().getPlayerOrThrow());
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.player())
                .requires(source -> this.permission(source, "seam.topother", 2))
                .executes(context -> {
                    this.top(context.getSource(), EntityArgumentType.getPlayer(context, "target"));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private void top(ServerCommandSource source, ServerPlayerEntity player) {

        








    }
}

package me.novoro.seam.commands.utility;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.novoro.seam.utils.ColorUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

/**
 * Provides a command to broadcast messages to all players on the server.
 */
public class BroadcastCommand extends CommandBase {
    public BroadcastCommand() {
        super("broadcast", "seam.broadcast", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(
                CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String message = StringArgumentType.getString(ctx, "message");
                            return broadcastMessage(ctx.getSource(), message);
                        })
        );
    }

    /**
     * Broadcasts a message to all players on the server.
     *
     * @param source  The source of the command.
     * @param message The message to broadcast.
     * @return 1 if successful, 0 otherwise.
     */
    private static int broadcastMessage(ServerCommandSource source, String message) {
        Collection<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();

        String prefix = LangManager.getLang("Broadcast-Prefix");

        String formattedMessage = prefix + message;
        Component adventureComponent = ColorUtil.parseColour(formattedMessage);

        for (ServerPlayerEntity player : players) {
            Audience audience = player;
            audience.sendMessage(adventureComponent);
        }

        return 1;
    }
}

package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.api.Location;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

public class TPHereCommand extends CommandBase {
    public TPHereCommand() {
        super("tphere", "seam.tphere", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("target", EntityArgumentType.players())
                .executes(context -> {
                    ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
                    Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "target");
                    Location senderLocation = new Location(sender);
                    targets.forEach(target -> {
                        senderLocation.teleport(target);
                        LangManager.sendLang(target, "TPHere-Target-Message", Map.of("{player}", sender.getName().getString()));
                    });
                    if (targets.size() == 1) {
                        ServerPlayerEntity first = targets.iterator().next();
                        LangManager.sendLang(context.getSource(), "TPHere-Sender-Message", Map.of("{player}", first.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "TPHere-All-Message", Map.of("{amount}", String.valueOf(targets.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}

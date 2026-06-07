package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.SettingsManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
 
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
//TODO: Maybe make radius configurable via permissions?
public class NearCommand extends CommandBase {
    public NearCommand() {
        super("near", "seam.near", 2);
    }
 
    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            int radius = SettingsManager.getNearRadius();
 
            List<ServerPlayerEntity> nearby = player.getServerWorld().getPlayers().stream()
                    .filter(p -> !p.getUuid().equals(player.getUuid()))
                    .filter(p -> p.distanceTo(player) <= radius)
                    .sorted(Comparator.comparingDouble(p -> p.distanceTo(player)))
                    .collect(Collectors.toList());
 
            if (nearby.isEmpty()) {
                LangManager.sendLang(context.getSource(), "Near-None", Map.of("{radius}", String.valueOf(radius)));
                return Command.SINGLE_SUCCESS;
            }
 
            String playerList = nearby.stream()
                    .map(p -> p.getName().getString() + " (" + (int) p.distanceTo(player) + " blocks)")
                    .collect(Collectors.joining(", "));
 
            LangManager.sendLang(context.getSource(), "Near-Players", Map.of(
                    "{radius}", String.valueOf(radius),
                    "{players}", playerList
            ));
            return Command.SINGLE_SUCCESS;
        });
    }
}

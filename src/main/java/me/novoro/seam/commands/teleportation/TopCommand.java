package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.api.Location;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.TeleportationConfig;
import me.novoro.seam.utils.LocationUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class TopCommand extends CommandBase {
    public TopCommand() {
        super("top", "seam.top", 2);
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
        int highestAscendY = TeleportationConfig.getHighestAscendY(player.getServerWorld());
        Location highest = new Location(player);
        highest.setY(highestAscendY);
        Location surface = LocationUtil.getNextSafeBelow(highest, true);
        String lang;
        Map<String, String> replacements = new HashMap<>();
        if (surface == null) {
            lang = "Top-Unsafe-Message";
        } else if (surface.getY() <= player.getY()) {
            lang = "Top-Already-Highest";
        } else {
            surface.teleport(player);
            surface.addReplacements(replacements);
            lang = "Top-Success-Message";
        }
        if (source.getPlayer() != player) {
            lang += "-Other";
            replacements.put("{target}", player.getName().getString());
        }
        LangManager.sendLang(source, lang, replacements);
    }
}

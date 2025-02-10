package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class AscendCommand extends CommandBase {
    public AscendCommand() {
        super("ascend", "seam.ascend", 1);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            this.ascend(context.getSource(), context.getSource().getPlayerOrThrow(), 1);
            return Command.SINGLE_SUCCESS;
        }).then(argument("levels", IntegerArgumentType.integer(1))
                .executes(context -> {
                    this.ascend(context.getSource(),
                            context.getSource().getPlayerOrThrow(),
                            context.getArgument("levels", Integer.class));
                    return Command.SINGLE_SUCCESS;
                }).then(argument("target", EntityArgumentType.player())
                        .requires(source -> this.permission(source, "seam.ascendother", 2))
                        .executes(context -> {
                            this.ascend(context.getSource(),
                                    EntityArgumentType.getPlayer(context, "target"),
                                    context.getArgument("levels", Integer.class));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    private void ascend(ServerCommandSource source, ServerPlayerEntity player, int floors) {
        int highestAscendY = TeleportationConfig.getHighestAscendY(player.getServerWorld());
        Location playerLocation = new Location(player), target = null;
        int floor;
        for (floor = 0; floor < floors; floor++) {
            Location last = (target == null) ? playerLocation : target;
            Location next = LocationUtil.getNextSafeAbove(last);
            if (next == null || next.getY() > highestAscendY) break;
            target = next;
        }
        String lang;
        Map<String, String> replacements = new HashMap<>();
        if (target == null) {
            lang = "Ascend-Location-Not-Found";
        } else {
            lang = (floor == 1) ? "Ascended-Message" : "Ascended-Floors";
            target.teleport(player);
            target.addReplacements(replacements);
            if (floor > 1) replacements.put("{floor}", String.valueOf(floor));
        }
        if (source.getPlayer() != player) {
            lang += "-Other";
            replacements.put("{target}", player.getName().getString());
        }
        LangManager.sendLang(source, lang, replacements);
    }
}

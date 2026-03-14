package me.novoro.seam.commands.teleportation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.api.Location;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.config.PlayerStorageManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class BackCommand extends CommandBase {
    public BackCommand() {
        super("back", "seam.back", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            return back(context.getSource(), player, false);
        }).then(argument("target", EntityArgumentType.player())
                .requires(source -> this.permission(source, "seam.back.other", 3))
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    return back(context.getSource(), target, true);
                })
        );
    }

    private int back(ServerCommandSource source, ServerPlayerEntity target, boolean isOther) {
        Location lastLocation = PlayerStorageManager.get(target.getUuid()).getPreviousLocation();

        if (lastLocation == null) {
            if (isOther) LangManager.sendLang(source, "Back-No-Location-Other", Map.of("{player}", target.getName().getString()));
            else LangManager.sendLang(source, "Back-No-Location");
            return 0;
        }

        lastLocation.teleport(target);
        if (isOther) {
            LangManager.sendLang(target, "Back-Teleport");
            LangManager.sendLang(source, "Back-Teleport-Other", Map.of("{player}", target.getName().getString()));
        } else LangManager.sendLang(source, "Back-Teleport");
        return Command.SINGLE_SUCCESS;
    }
}

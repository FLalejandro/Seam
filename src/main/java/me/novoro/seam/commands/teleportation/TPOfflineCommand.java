package me.novoro.seam.commands.teleportation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.api.Location;
import me.novoro.seam.utils.GameProfileUtil;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class TPOfflineCommand extends CommandBase {
    public TPOfflineCommand() {
        super("tpoffline", "seam.tpoffline", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("target", GameProfileArgumentType.gameProfile())
                .executes(context -> {
                    ServerPlayerEntity sender = context.getSource().getPlayerOrThrow();
                    GameProfile target = GameProfileArgumentType.getProfileArgument(context, "target").iterator().next();
                    Location offlineLocation;

                    if (Objects.requireNonNull(sender.getServer()).getPlayerManager().getPlayer(target.getId()) != null) {
                        LangManager.sendLang(sender, "TPOffline-Online-Notify", Map.of("{player}", target.getName()));
                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        offlineLocation = GameProfileUtil.getOfflineLocation(target);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (offlineLocation == null) {
                        LangManager.sendLang(sender, "TPOffline-Error", Map.of("{player}", target.getName()));
                        return Command.SINGLE_SUCCESS;
                    }

                    offlineLocation.teleport(sender);
                    LangManager.sendLang(sender, "TPOffline-Success", Map.of("{player}", target.getName()));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
package me.novoro.seam.commands.ability;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import java.util.Map;

public abstract class GamemodeCommand extends CommandBase {
    private final GameMode gameMode;

    protected GamemodeCommand(String name, String permission, GameMode gameMode, String... aliases) {
        super(name, permission, 2, aliases);
        this.gameMode = gameMode;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command
                .executes(context -> execute(context.getSource().getPlayerOrThrow(), context.getSource()))
                .then(argument("target", EntityArgumentType.player())
                        .requires(source -> this.permission(source, "minecraft.command.gamemode", 2))
                        .executes(context -> execute(EntityArgumentType.getPlayer(context, "target"), context.getSource())));
    }

    private int execute(ServerPlayerEntity target, ServerCommandSource source) {
        target.changeGameMode(gameMode);

        if (target == source.getPlayer()) LangManager.sendLang(source, "GameMode-Self-Message", Map.of("{gamemode}", gameMode.getTranslatableName().getString()));
        else {
            LangManager.sendLang(source, "GameMode-Other-Message", Map.of("{player}", target.getName().getString(), "{gamemode}", gameMode.getTranslatableName().getString()));
            LangManager.sendLang(target, "GameMode-Self-Message", Map.of("{gamemode}", gameMode.getTranslatableName().getString()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
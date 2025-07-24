package me.novoro.seam.commands.ability;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

/**
 * Provides command to toggle lock inventory mode for players.
 * "Locking" an inventory means players can not pick up items when this is toggled.
 */
public class LockInventoryCommand extends CommandBase {
    public LockInventoryCommand() {
        super("lockinventory", "seam.lockinventory", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            boolean wasEnabled = toggleLockInventory(player);
            LangManager.sendLang(context.getSource(), wasEnabled ? "LockInventory-Disabled-Message" : "LockInventory-Enabled-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.lockinventorytargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        boolean wasEnabled = toggleLockInventory(player);
                        LangManager.sendLang(player, wasEnabled ? "LockInventory-Disabled-Message" : "LockInventory-Enabled-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity first = players.iterator().next();
                        String messageKey = first.getAbilities().allowLockInventory ? "LockInventory-Other-Enabled-Message" : "LockInventory-Other-Disabled-Message";
                        LangManager.sendLang(context.getSource(), messageKey, Map.of("{player}", first.getName().getString()));
                    } else {
                        LangManager.sendLang(context.getSource(), "LockInventory-All-Message", Map.of("{amount}", String.valueOf(players.size())));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Toggles Lock Inventory mode on the target player.
     *
     * @param target the target players.
     * @return true if Lock Inventory mode is enabled after toggling, false otherwise.
     */
    // ToDo: Move toggles to player data
    private static boolean toggleLockInventory(ServerPlayerEntity target) {
        boolean isEnabled = !target.getAbilities().allowLockInventory;
        target.getAbilities().allowLockInventory = isEnabled;
        target.sendAbilitiesUpdate();
        return isEnabled;
    }
}
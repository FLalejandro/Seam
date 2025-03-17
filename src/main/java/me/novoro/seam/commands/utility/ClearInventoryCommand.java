package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

/**
 * Provides command to clear the player's inventory.
 */
public class ClearInventoryCommand extends CommandBase {
    public ClearInventoryCommand() {
        super("clearinventory", "seam.clearinventory", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ClearInventoryCommand.clearInventory(context.getSource().getPlayerOrThrow());
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.clearinventorytargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(ClearInventoryCommand::clearInventory);
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "ClearInventory-Other-Message", Map.of("{player}", firstPlayer.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "ClearInventory-All-Message", Map.of("{amount}", String.valueOf(players.size())));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Clears the inventory of each target player.
     *
     * @param targets The target players.
     */
    private static void clearInventory(ServerPlayerEntity... targets) {
        for (ServerPlayerEntity target : targets) {
            // Clear main inventory
            target.getInventory().clear();

            // Clear armor slots
            for (int i = 0; i < target.getInventory().armor.size(); i++) {
                target.getInventory().armor.set(i, ItemStack.EMPTY);
            }

            // Clear off-hand slot
            target.getInventory().offHand.set(0, ItemStack.EMPTY);

            // Send message to the player
            LangManager.sendLang(target, "ClearInventory-Self-Message");
        }
    }
}
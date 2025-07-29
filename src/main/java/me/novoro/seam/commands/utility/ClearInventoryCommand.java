package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.SettingsManager;
import me.novoro.seam.config.LangManager;
import me.novoro.seam.utils.TimeUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides command to clear the player's inventory.
 */
public class ClearInventoryCommand extends CommandBase {
    public ClearInventoryCommand() {
        super("clearinventory", "seam.clearinventory", 2, "ci");
    }

    private static final Map<ServerPlayerEntity, Long> clearInventoryConfirmations = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            String commandUsed = "/" + context.getInput().split(" ")[0];
            if (!SettingsManager.clearInventoryRequiresConfirmation()) ClearInventoryCommand.clearInventory(context.getSource().getPlayerOrThrow());
            else clearInventoryConfirmation(context.getSource().getPlayerOrThrow(), commandUsed);
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
     * @param target The target players.
     */
    private static void clearInventory(ServerPlayerEntity target) {
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

    /**
     * Clear Inventory Confirmation
     */
    private static void clearInventoryConfirmation(ServerPlayerEntity player, String commandAlias) {
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = SettingsManager.getClearInventoryConfirmationTimeoutMillis();

        clearInventoryConfirmations.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > timeoutMillis
        );

        if (clearInventoryConfirmations.containsKey(player)) {
            clearInventory(player);
            clearInventoryConfirmations.remove(player);
        } else {
            clearInventoryConfirmations.put(player, currentTime);
            LangManager.sendLang(player, "ClearInventory-Confirmation", Map.of("{command}", commandAlias,
                    "{time}", TimeUtil.getFormattedTime(TimeUnit.MILLISECONDS.toSeconds(timeoutMillis))));
        }
    }
}
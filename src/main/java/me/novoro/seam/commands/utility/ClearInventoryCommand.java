package me.novoro.seam.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
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
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            ClearInventory(player);
            LangManager.sendLang(context.getSource(), "ClearInventory-Self-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "seam.clearinventorytargets", 4))
                .executes(context -> {
                    List<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target").stream().toList();
                    for (ServerPlayerEntity player : players) {
                        ClearInventory(player);
                        LangManager.sendLang(player, "ClearInventory-Self-Message");
                    }
                    if (players.size() == 1) {
                        String firstPlayer = players.getFirst().getName().getString();
                        LangManager.sendLang(context.getSource(), "ClearInventory-Other-Message", Map.of("{player}", firstPlayer));
                    } else {
                        LangManager.sendLang(context.getSource(), "ClearInventory-All-Message", Map.of("{amount}", String.valueOf(players.size())));
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Clears the inventory the target player(s).
     *
     * @param ctx     The command context.
     * @param targets The target players.
     */
    private static void ClearInventory(ServerPlayerEntity ctx, ServerPlayerEntity... targets) {
        ServerCommandSource source = ctx.getCommandSource();
        ServerPlayerEntity player = targets.length > 0 ? targets[0] : source.getPlayer();

        if (player == null) return;

        // Clearing main inventory
        player.getInventory().clear();

        // Clearing armor slots
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }

        // Clearing off-hand slot
        player.getInventory().offHand.set(0, ItemStack.EMPTY);

    }
}


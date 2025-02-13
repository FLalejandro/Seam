package me.novoro.seam.commands.inventory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.enums.ScreenType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class InventoryCommand extends CommandBase {

    // Utilizing Neo's Enum for ScreenType ^^
    private final ScreenType type;
    private final Identifier stat;
    private final String translationKey;

    /**
     * @param name           The command name, ex: "anvil".
     * @param permission     The permission node, ex: "seam.anvil".
     * @param type           The custom screen type to open (from our enum).
     * @param stat           The relevant statistic to increment (e.g., Stats.INTERACT_WITH_ANVIL).
     * @param translationKey The translation key for the screen title.
     */
    protected InventoryCommand(String name, String permission, ScreenType type, Identifier stat, String translationKey) {
        super(name, permission, 4);
        this.type = type;
        this.stat = stat;
        this.translationKey = translationKey;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(ctx -> openInventory(ctx.getSource()));
    }

    /**
     * Opens the specified inventory screen for the player executing the command.
     */
    private int openInventory(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        assert player != null;

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, playerEntity) ->
                        type.createHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())),
                Text.translatable(translationKey)
        ));

        // Increment the relevant stat if applicable.
        if (stat != null) {
            player.incrementStat(stat);
        }
        return Command.SINGLE_SUCCESS;
    }
}

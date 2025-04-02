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

public abstract class InventoryCommand extends CommandBase {

    private final ScreenType type;
    private final Identifier stat;
    private final Text screenTitle;

    /**
     * @param name           The command name, ex: "anvil".
     * @param permission     The permission node, ex: "seam.anvil".
     * @param type           The custom screen type to open (from our enum).
     * @param stat           The relevant statistic to increment (e.g., Stats.INTERACT_WITH_ANVIL).
     * @param screenTitle    The screen's title.
     */
    protected InventoryCommand(String name, String permission, ScreenType type, Identifier stat, Text screenTitle, String... aliases) {
        super(name, permission, 4, aliases);
        this.type = type;
        this.stat = stat;
        this.screenTitle = screenTitle;
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
                        this.type.createHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())),
                this.screenTitle
        ));

        if (this.stat != null) player.incrementStat(this.stat);

        return Command.SINGLE_SUCCESS;
    }
}

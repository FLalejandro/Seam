package me.novoro.seam.commands.inventories;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.seam.commands.CommandBase;
import me.novoro.seam.config.LangManager;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Registers all inventory-related commands under one class.
 */
public final class InventoryCommands {


    private static abstract class InventoryCommand extends CommandBase {
        private final InventoryScreens.ScreenType type;
        private final Identifier stat;
        private final String translationKey;

        protected InventoryCommand(String name, String permission, InventoryScreens.ScreenType type, Identifier stat, String translationKey) {
            super(name, permission, 2);
            this.type = type;
            this.stat = stat;
            this.translationKey = translationKey;
        }

        @Override
        public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
            return command.executes(ctx -> openInventory(ctx.getSource()));
        }

        private int openInventory(ServerCommandSource source) {
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) return 0;

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inventory, playerEntity) ->
                            new InventoryScreens<>(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()), type),
                    Text.translatable(translationKey)
            ));

            if (stat != null) {
                player.incrementStat(stat);
            }

            LangManager.sendLang(source, "Inventory-Opened", Map.of("{inventory}", translationKey));
            return Command.SINGLE_SUCCESS;
        }
    }

    public static class AnvilCommand extends InventoryCommand {
        public AnvilCommand() {
            super("anvil", "seam.anvil", InventoryScreens.ScreenType.ANVIL, Stats.INTERACT_WITH_ANVIL, "container.repair");
        }
    }

    public static class CartographyCommand extends InventoryCommand {
        public CartographyCommand() {
            super("cartography", "seam.cartography", InventoryScreens.ScreenType.CARTOGRAPHY, Stats.INTERACT_WITH_CARTOGRAPHY_TABLE, "container.cartography_table");
        }
    }

    public static class EnchantmentTableCommand extends InventoryCommand {
        public EnchantmentTableCommand() {
            super("enchantmenttable", "seam.enchantmenttable", InventoryScreens.ScreenType.ENCHANTMENT, null, "container.enchant");
        }
    }

    public static class GrindstoneCommand extends InventoryCommand {
        public GrindstoneCommand() {
            super("grindstone", "seam.grindstone", InventoryScreens.ScreenType.GRINDSTONE, Stats.INTERACT_WITH_GRINDSTONE, "container.grindstone_title");
        }
    }

    public static class LoomCommand extends InventoryCommand {
        public LoomCommand() {
            super("loom", "seam.loom", InventoryScreens.ScreenType.LOOM, Stats.INTERACT_WITH_LOOM, "container.loom");
        }
    }

    public static class SmithingCommand extends InventoryCommand {
        public SmithingCommand() {
            super("smithing", "seam.smithing", InventoryScreens.ScreenType.SMITHING, Stats.INTERACT_WITH_SMITHING_TABLE, "container.upgrade");
        }
    }

    public static class StonecutterCommand extends InventoryCommand {
        public StonecutterCommand() {
            super("stonecutter", "seam.stonecutter", InventoryScreens.ScreenType.STONECUTTER, Stats.INTERACT_WITH_STONECUTTER, "container.stonecutter");
        }
    }

    public static class WorkbenchCommand extends InventoryCommand {
        public WorkbenchCommand() {
            super("workbench", "seam.workbench", InventoryScreens.ScreenType.WORKBENCH, Stats.INTERACT_WITH_CRAFTING_TABLE, "container.crafting");
        }
    }
}

package me.novoro.seam.commands.inventories;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;

/**
 * ScreenHandler that sorts between Vanilla (ex: Workbench)
 * and Custom Screens (ex: Disposal)
 */
public class InventoryScreens extends ScreenHandler {

    /**
     * Types of screen/container to be created.
     */
    public enum ScreenType {
        ANVIL,
        CARTOGRAPHY,
        ENCHANTMENT,
        GRINDSTONE,
        LOOM,
        SMITHING,
        STONECUTTER,
        WORKBENCH
    }

    /**
     * For Custom Gooeys like Disposal/Invsee
     */
    public InventoryScreens(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ScreenType type) {
        super(getScreenHandlerType(type), syncId);

    }

    /**
     * Creates the appropriate ScreenHandler for each ScreenType.
     */
    public static ScreenHandler createHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ScreenType type) {
        return switch (type) {
            case ANVIL -> new SeamAnvilScreenHandler(syncId, playerInventory, context);
            case CARTOGRAPHY -> new SeamCartographyScreenHandler(syncId, playerInventory, context);
            case ENCHANTMENT -> new SeamEnchantmentScreenHandler(syncId, playerInventory, context);
            case GRINDSTONE -> new SeamGrindstoneScreenHandler(syncId, playerInventory, context);
            case LOOM -> new SeamLoomScreenHandler(syncId, playerInventory, context);
            case SMITHING -> new SeamSmithingScreenHandler(syncId, playerInventory, context);
            case STONECUTTER -> new SeamStonecutterScreenHandler(syncId, playerInventory, context);
            case WORKBENCH -> new SeamWorkbenchScreenHandler(syncId, playerInventory, context);
        };
    }

    /**
     * Returns the appropriate ScreenHandlerType for each ScreenType.
     *
     */
    private static ScreenHandlerType<?> getScreenHandlerType(ScreenType type) {
        return switch (type) {
            case ANVIL        -> ScreenHandlerType.ANVIL;
            case CARTOGRAPHY  -> ScreenHandlerType.CARTOGRAPHY_TABLE;
            case ENCHANTMENT  -> ScreenHandlerType.ENCHANTMENT;
            case GRINDSTONE   -> ScreenHandlerType.GRINDSTONE;
            case LOOM         -> ScreenHandlerType.LOOM;
            case SMITHING     -> ScreenHandlerType.SMITHING;
            case STONECUTTER  -> ScreenHandlerType.STONECUTTER;
            case WORKBENCH    -> ScreenHandlerType.CRAFTING;
        };
    }

    /**
     * Returns how many slots we track (We track this to prevent dupes/item loss)
     */
    private static int getSlotCount(ScreenType type) {
        return switch (type) {
            case STONECUTTER                 -> 1;
            case ANVIL, GRINDSTONE           -> 2;
            case CARTOGRAPHY, ENCHANTMENT,
                 LOOM, SMITHING             -> 3;
            case WORKBENCH                   -> 10;
        };
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    public static class SeamAnvilScreenHandler extends AnvilScreenHandler {
        public SeamAnvilScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamCartographyScreenHandler extends CartographyTableScreenHandler {
        public SeamCartographyScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamEnchantmentScreenHandler extends EnchantmentScreenHandler {
        public SeamEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamGrindstoneScreenHandler extends GrindstoneScreenHandler {
        public SeamGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamLoomScreenHandler extends LoomScreenHandler {
        public SeamLoomScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamSmithingScreenHandler extends SmithingScreenHandler {
        public SeamSmithingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamStonecutterScreenHandler extends StonecutterScreenHandler {
        public SeamStonecutterScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class SeamWorkbenchScreenHandler extends CraftingScreenHandler {
        public SeamWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(syncId, playerInventory, context);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }
}

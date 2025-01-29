package me.novoro.seam.commands.inventories;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;

public class InventoryScreens<T extends ScreenHandler> extends ScreenHandler {

    private final T parentHandler;
    private final int slotCount;

    public enum ScreenType {
        ANVIL, CARTOGRAPHY, ENCHANTMENT, DISPOSAL, ENDERCHEST, GRINDSTONE, LOOM, SMITHING, STONECUTTER, WORKBENCH
    }

    public InventoryScreens(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ScreenType type) {
        super(getScreenHandlerType(type), syncId);
        this.parentHandler = createHandler(syncId, playerInventory, context, type);
        this.slotCount = getSlotCount(type);
    }

    private static <T extends ScreenHandler> T createHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ScreenType type) {
        return switch (type) {
            case ANVIL -> (T) new AnvilScreenHandler(syncId, playerInventory, context);
            case CARTOGRAPHY -> (T) new CartographyTableScreenHandler(syncId, playerInventory, context);
            case ENCHANTMENT -> (T) new EnchantmentScreenHandler(syncId, playerInventory, context);
            case DISPOSAL -> null;
            case ENDERCHEST -> null;
            case GRINDSTONE -> (T) new GrindstoneScreenHandler(syncId, playerInventory, context);
            case LOOM -> (T) new LoomScreenHandler(syncId, playerInventory, context);
            case SMITHING -> (T) new SmithingScreenHandler(syncId, playerInventory, context);
            case STONECUTTER -> (T) new StonecutterScreenHandler(syncId, playerInventory, context);
            case WORKBENCH -> (T) new CraftingScreenHandler(syncId, playerInventory, context);
        };
    }

    private static ScreenHandlerType<?> getScreenHandlerType(ScreenType type) {
        return switch (type) {
            case ANVIL -> ScreenHandlerType.ANVIL;
            case CARTOGRAPHY -> ScreenHandlerType.CARTOGRAPHY_TABLE;
            case ENCHANTMENT -> ScreenHandlerType.ENCHANTMENT;
            case DISPOSAL -> null;
            case ENDERCHEST -> null;
            case GRINDSTONE -> null;
            case LOOM -> null;
            case SMITHING -> null;
            case STONECUTTER -> null;
            case WORKBENCH -> null;
        };
    }

    private static int getSlotCount(ScreenType type) {
        return switch (type) {
            case STONECUTTER -> 1;
            case ANVIL, GRINDSTONE -> 2;
            case CARTOGRAPHY, ENCHANTMENT, LOOM, SMITHING -> 3;
            case WORKBENCH -> 10;
            case DISPOSAL, ENDERCHEST -> 0;
        };
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        // Dump items back to player or drop on ground
        for (int i = 1; i < slotCount; i++) {
            Slot slot = this.getSlot(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                if (!player.giveItemStack(stack)) {
                    player.dropItem(stack, false);
                }
                slot.setStack(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        return ItemStack.EMPTY;
    }
}

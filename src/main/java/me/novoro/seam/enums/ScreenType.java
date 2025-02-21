package me.novoro.seam.enums;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.*;

public enum ScreenType {
    ANVIL((syncID, playerInventory, context) -> new AnvilScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 2),
    CARTOGRAPHY((syncID, playerInventory, context) -> new CartographyTableScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 3),
    DISPOSAL((syncID, playerInventory, context) ->
            new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncID, playerInventory, new SimpleInventory(27), 3),
            0),
    ENCHANTMENT((syncID, playerInventory, context) -> new EnchantmentScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 3),
    ENDERCHEST((syncID, playerInventory, context) ->
            new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncID, playerInventory, new SimpleInventory(27), 3),
            0),
    GRINDSTONE((syncID, playerInventory, context) -> new GrindstoneScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 2),
    INVSEE((syncID, playerInventory, context) ->
            new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncID, playerInventory, new SimpleInventory(36), 4),
            0),
    LOOM((syncID, playerInventory, context) -> new LoomScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 3),
    SMITHING((syncID, playerInventory, context) -> new SmithingScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 3),
    STONECUTTER((syncID, playerInventory, context) -> new StonecutterScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 1),
    WORKBENCH((syncID, playerInventory, context) -> new CraftingScreenHandler(syncID, playerInventory, context) {
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }, 10);

    private final TriFunction<Integer, PlayerInventory, ScreenHandlerContext, ScreenHandler> creatorFunction;
    private final int trackedSlots;

    ScreenType(TriFunction<Integer, PlayerInventory, ScreenHandlerContext, ScreenHandler> creatorFunction, int trackedSlots) {
        this.creatorFunction = creatorFunction;
        this.trackedSlots = trackedSlots;
    }

    public ScreenHandler createHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        return this.creatorFunction.apply(syncId, playerInventory, context);
    }

    public int getTrackedSlots() {
        return this.trackedSlots;
    }

    private interface TriFunction<P1, P2, P3, R> {
        R apply(P1 one, P2 two, P3 three);
    }
}
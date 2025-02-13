package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;

public class CartographyCommand extends InventoryCommand {
    public CartographyCommand() {
        super("cartography", "seam.cartography", ScreenType.CARTOGRAPHY, Stats.INTERACT_WITH_CARTOGRAPHY_TABLE, "container.cartography_table");
    }
}
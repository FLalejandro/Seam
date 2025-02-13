package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class CartographyCommand extends InventoryCommand {
    public CartographyCommand() {
        super("cartography", "seam.cartography", ScreenType.CARTOGRAPHY, Stats.INTERACT_WITH_CARTOGRAPHY_TABLE, Text.translatable("container.cartography_table"));
    }
}
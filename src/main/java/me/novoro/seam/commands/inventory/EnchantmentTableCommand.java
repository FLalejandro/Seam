package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;

public class EnchantmentTableCommand extends InventoryCommand {
    public EnchantmentTableCommand() {
        super("enchantmenttable", "seam.enchantmenttable", ScreenType.ENCHANTMENT, null, "container.enchant");
    }
}

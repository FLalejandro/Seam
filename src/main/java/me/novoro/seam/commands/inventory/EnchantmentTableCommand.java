package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.text.Text;

public class EnchantmentTableCommand extends InventoryCommand {
    public EnchantmentTableCommand() {
        super("enchantmenttable", "seam.enchantmenttable", ScreenType.ENCHANTMENT, null, Text.translatable("container.enchant"));
    }
}

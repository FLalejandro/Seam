package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.text.Text;

public class InvseeCommand extends InventoryCommand {
    public InvseeCommand() {
        super("invsee", "seam.invsee", ScreenType.ENDERCHEST, null, Text.of("Player's Inventory"));
    }
}

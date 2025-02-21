package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.text.Text;

public class EnderChestCommand extends InventoryCommand {
    public EnderChestCommand() {
        super("enderchest", "seam.enderchest", ScreenType.ENDERCHEST, null, Text.translatable("container.enderchest"));
    }
}

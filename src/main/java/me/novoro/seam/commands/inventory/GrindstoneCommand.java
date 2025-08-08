package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class GrindstoneCommand extends InventoryCommand {
    public GrindstoneCommand() {
        super("grindstone", "seam.grindstone", ScreenType.GRINDSTONE, Stats.INTERACT_WITH_GRINDSTONE, Text.translatable("container.grindstone_title"));
    }
}

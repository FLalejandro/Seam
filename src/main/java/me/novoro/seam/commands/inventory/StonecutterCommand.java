package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;

public class StonecutterCommand extends InventoryCommand {
    public StonecutterCommand() {
        super("stonecutter", "seam.stonecutter", ScreenType.STONECUTTER, Stats.INTERACT_WITH_STONECUTTER, "container.stonecutter");
    }
}

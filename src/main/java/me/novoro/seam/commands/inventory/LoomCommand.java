package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;

public class LoomCommand extends InventoryCommand {
    public LoomCommand() {
        super("loom", "seam.loom", ScreenType.LOOM, Stats.INTERACT_WITH_LOOM, "container.loom");
    }
}

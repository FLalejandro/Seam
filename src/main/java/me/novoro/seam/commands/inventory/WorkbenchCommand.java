package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;

public class WorkbenchCommand extends InventoryCommand {
    public WorkbenchCommand() {
        super("workbench", "seam.workbench", ScreenType.WORKBENCH, Stats.INTERACT_WITH_CRAFTING_TABLE, "container.crafting");
    }
}

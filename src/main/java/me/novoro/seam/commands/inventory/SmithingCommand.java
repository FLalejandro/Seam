package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;

public class SmithingCommand extends InventoryCommand {
    public SmithingCommand() {
        super("smithing", "seam.smithing", ScreenType.SMITHING, Stats.INTERACT_WITH_SMITHING_TABLE, "container.upgrade");
    }
}

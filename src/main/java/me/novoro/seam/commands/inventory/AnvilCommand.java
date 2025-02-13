package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;

public class AnvilCommand extends InventoryCommand {
    public AnvilCommand() {
        super("anvil", "seam.anvil", ScreenType.ANVIL, Stats.INTERACT_WITH_ANVIL, "container.repair");
    }
}

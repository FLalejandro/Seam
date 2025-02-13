package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class SmithingCommand extends InventoryCommand {
    public SmithingCommand() {
        super("smithing", "seam.smithing", ScreenType.SMITHING, Stats.INTERACT_WITH_SMITHING_TABLE, Text.translatable("container.upgrade"));
    }
}

package me.novoro.seam.commands.inventory;

import me.novoro.seam.enums.ScreenType;
import net.minecraft.text.Text;

public class DisposalCommand extends InventoryCommand {
    public DisposalCommand() {
        super("disposal", "seam.disposal", ScreenType.DISPOSAL, null, Text.of("Disposal"));
    }
}

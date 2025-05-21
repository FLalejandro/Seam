package me.novoro.seam.commands.ability;

import net.minecraft.world.GameMode;

public class CreativeCommand extends GamemodeCommand {
    public CreativeCommand() {
        super("gmc", "minecraft.command.gamemode", GameMode.CREATIVE);
    }
}
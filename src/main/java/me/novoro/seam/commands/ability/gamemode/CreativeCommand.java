package me.novoro.seam.commands.ability.gamemode;

import net.minecraft.world.GameMode;

public class CreativeCommand extends GamemodeCommand {
    public CreativeCommand() {
        super("creative", "minecraft.command.gamemode", GameMode.CREATIVE, "gmc");
    }
}
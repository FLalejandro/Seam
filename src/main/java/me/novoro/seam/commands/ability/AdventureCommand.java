package me.novoro.seam.commands.ability;

import net.minecraft.world.GameMode;

public class AdventureCommand extends GamemodeCommand {
    public AdventureCommand() {
        super("gma", "minecraft.command.gamemode", GameMode.ADVENTURE);
    }
}
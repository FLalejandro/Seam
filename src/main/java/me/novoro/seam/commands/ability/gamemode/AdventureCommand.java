package me.novoro.seam.commands.ability.gamemode;

import net.minecraft.world.GameMode;

public class AdventureCommand extends GamemodeCommand {
    public AdventureCommand() {
        super("adventure", "minecraft.command.gamemode", GameMode.ADVENTURE, "gma");
    }
}